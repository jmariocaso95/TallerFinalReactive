package com.example.taller.service;

import com.example.taller.common.AppProperties;
import com.example.taller.common.CorrelationWebFilter;
import com.example.taller.common.DomainExceptions.DespachoNoExisteException;
import com.example.taller.common.DomainExceptions.EstadoInvalidoException;
import com.example.taller.common.DomainExceptions.ZonaRiesgosaException;
import com.example.taller.common.EstadoPaquete;
import com.example.taller.dto.DespachoRequest;
import com.example.taller.dto.DespachoResponse;
import com.example.taller.dto.EventoDespacho;
import com.example.taller.dto.PaqueteRequest;
import com.example.taller.external.ClimaClient;
import com.example.taller.external.PricingClient;
import com.example.taller.external.RiesgoClient;
import com.example.taller.model.Despacho;
import com.example.taller.model.Paquete;
import com.example.taller.repository.DespachoRepository;
import com.example.taller.repository.PaqueteRepository;
import com.example.taller.service.VehiculoReservaService.ReservaHecha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * Orquesta el saga de creación de un despacho: RECIBIDO -> (reserva de cupo) -> (tarifa/clima/riesgo
 * en paralelo) -> ASIGNADO (o RECHAZADO, con compensación) -> EN_RUTA al confirmar.
 */
@Service
public class DespachoService {

    private static final Logger log = LoggerFactory.getLogger(DespachoService.class);

    private final DespachoRepository despachoRepository;
    private final PaqueteRepository paqueteRepository;
    private final VehiculoReservaService reservaService;
    private final PricingClient pricingClient;
    private final ClimaClient climaClient;
    private final RiesgoClient riesgoClient;
    private final EventBusService eventBus;
    private final TransactionalOperator tx;
    private final AppProperties props;

    public DespachoService(DespachoRepository despachoRepository, PaqueteRepository paqueteRepository,
                            VehiculoReservaService reservaService, PricingClient pricingClient,
                            ClimaClient climaClient, RiesgoClient riesgoClient, EventBusService eventBus,
                            TransactionalOperator tx, AppProperties props) {
        this.despachoRepository = despachoRepository;
        this.paqueteRepository = paqueteRepository;
        this.reservaService = reservaService;
        this.pricingClient = pricingClient;
        this.climaClient = climaClient;
        this.riesgoClient = riesgoClient;
        this.eventBus = eventBus;
        this.tx = tx;
        this.props = props;
    }

    public Flux<Despacho> findAll() {
        return despachoRepository.findAll();
    }

    public Mono<DespachoResponse> obtener(Long id) {
        return despachoRepository.findById(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)))
                .flatMap(d -> paqueteRepository.findByDespachoId(id).collectList()
                        .map(paquetes -> DespachoResponse.de(d, paquetes)));
    }

    /** Crea un despacho ejecutando el saga completo. Idempotente por Idempotency-Key. */
    public Mono<DespachoResponse> crear(DespachoRequest request, String idemKey) {
        if (idemKey != null && !idemKey.isBlank()) {
            return despachoRepository.findByIdemKey(idemKey)
                    .flatMap(existente -> obtener(existente.getId()))
                    .switchIfEmpty(Mono.defer(() -> ejecutarSaga(request, idemKey)));
        }
        return ejecutarSaga(request, idemKey);
    }

    private Mono<DespachoResponse> ejecutarSaga(DespachoRequest request, String idemKey) {
        return Mono.deferContextual(ctx -> {
            String trazaId = ctx.getOrDefault(CorrelationWebFilter.KEY, "n/a");
            Despacho despacho = new Despacho(request.clienteId(), request.ciudad(), EstadoPaquete.RECIBIDO.name(),
                    null, null, null, trazaId, idemKey, Instant.now(), null);

            return despachoRepository.save(despacho)
                    .flatMap(guardado -> reservarYArmar(guardado, request))
                    .flatMap(this::evaluarYPersistir);
        });
    }

    private Mono<ContextoSaga> reservarYArmar(Despacho despacho, DespachoRequest request) {
        List<Integer> pesos = request.paquetes().stream().map(PaqueteRequest::pesoKg).toList();
        return reservaService.reservarParaPaquetes(despacho.getCiudad(), pesos)
                .onErrorResume(err -> marcarRechazado(despacho)
                        .then(Mono.error(err)))
                .map(reservas -> new ContextoSaga(despacho, request, reservas));
    }

    private Mono<DespachoResponse> evaluarYPersistir(ContextoSaga contexto) {
        Despacho despacho = contexto.despacho();
        return Mono.zip(
                        pricingClient.tarifaPara(despacho.getCiudad()),
                        climaClient.climaPara(despacho.getCiudad()),
                        riesgoClient.scorePara(despacho.getCiudad()))
                .flatMap(tupla -> {
                    double tarifa = tupla.getT1();
                    int score = tupla.getT3();
                    if (score > props.riskThreshold()) {
                        return reservaService.compensar(contexto.reservas())
                                .then(marcarRechazado(despacho))
                                .then(Mono.error(new ZonaRiesgosaException(score)));
                    }
                    return persistirAsignado(contexto, tarifa, score);
                });
    }

    private Mono<DespachoResponse> persistirAsignado(ContextoSaga contexto, double tarifa, int score) {
        Despacho despacho = contexto.despacho();
        List<PaqueteRequest> solicitados = contexto.request().paquetes();
        List<ReservaHecha> reservas = contexto.reservas();

        double totalValor = solicitados.stream().mapToDouble(p -> p.valor() == null ? 0.0 : p.valor()).sum();
        despacho.setEstado(EstadoPaquete.ASIGNADO.name());
        despacho.setTarifa(tarifa);
        despacho.setScoreRiesgo(score);
        despacho.setTotal(tarifa + totalValor);
        despacho.setExpiraEn(Instant.now().plus(props.reservationTtl()));

        List<Paquete> paquetes = new java.util.ArrayList<>();
        for (int i = 0; i < solicitados.size(); i++) {
            PaqueteRequest req = solicitados.get(i);
            Paquete p = new Paquete(despacho.getId(), reservas.get(i).vehiculo().getId(), req.pesoKg(), req.valor(), req.descripcion());
            paquetes.add(p);
        }

        Mono<Despacho> guardarDespacho = despachoRepository.save(despacho);
        Mono<List<Paquete>> guardarPaquetes = paqueteRepository.saveAll(paquetes).collectList();

        return guardarDespacho.then(guardarPaquetes)
                .as(tx::transactional)
                .doOnSuccess(ps -> eventBus.publicar(EventoDespacho.de(despacho.getId(),
                        despacho.getEstado(), "Despacho asignado", despacho.getTrazaId())))
                .map(ps -> DespachoResponse.de(despacho, ps));
    }

    private Mono<Despacho> marcarRechazado(Despacho despacho) {
        despacho.setEstado(EstadoPaquete.RECHAZADO.name());
        return despachoRepository.save(despacho)
                .doOnSuccess(d -> eventBus.publicar(EventoDespacho.de(d.getId(), d.getEstado(),
                        "Despacho rechazado", d.getTrazaId())));
    }

    /** ASIGNADO -> EN_RUTA: el cupo reservado se consume definitivamente. */
    public Mono<DespachoResponse> confirmar(Long id) {
        return despachoRepository.findById(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)))
                .flatMap(despacho -> {
                    if (!EstadoPaquete.ASIGNADO.name().equals(despacho.getEstado())) {
                        return Mono.error(new EstadoInvalidoException(
                                "El despacho " + id + " no está en estado ASIGNADO"));
                    }
                    return paqueteRepository.findByDespachoId(id).collectList()
                            .flatMap(paquetes -> Flux.fromIterable(paquetes)
                                    .concatMap(p -> reservaService.confirmar(p.getVehiculoId(), p.getPesoKg()))
                                    .then(Mono.just(paquetes)))
                            .flatMap(paquetes -> {
                                despacho.setEstado(EstadoPaquete.EN_RUTA.name());
                                despacho.setExpiraEn(null);
                                return despachoRepository.save(despacho)
                                        .doOnSuccess(d -> eventBus.publicar(EventoDespacho.de(d.getId(),
                                                d.getEstado(), "Despacho confirmado", d.getTrazaId())))
                                        .map(d -> DespachoResponse.de(d, paquetes));
                            });
                });
    }

    /** Contexto acumulado del saga mientras avanza por sus pasos. */
    private record ContextoSaga(Despacho despacho, DespachoRequest request, List<ReservaHecha> reservas) {}
}
