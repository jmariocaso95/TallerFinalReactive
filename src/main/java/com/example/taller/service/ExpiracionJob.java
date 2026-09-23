package com.example.taller.service;

import com.example.taller.common.AppProperties;
import com.example.taller.common.EstadoPaquete;
import com.example.taller.dto.EventoDespacho;
import com.example.taller.model.Despacho;
import com.example.taller.model.Paquete;
import com.example.taller.repository.DespachoRepository;
import com.example.taller.repository.PaqueteRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Cada app.expiry-interval (30 s por defecto) libera el cupo de los despachos ASIGNADOs
 * cuya reserva ya venció y los marca EXPIRADO.
 */
@Component
public class ExpiracionJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiracionJob.class);

    private final DespachoRepository despachoRepository;
    private final PaqueteRepository paqueteRepository;
    private final VehiculoReservaService reservaService;
    private final EventBusService eventBus;
    private final AppProperties props;

    private Disposable subscripcion;

    public ExpiracionJob(DespachoRepository despachoRepository, PaqueteRepository paqueteRepository,
                          VehiculoReservaService reservaService, EventBusService eventBus, AppProperties props) {
        this.despachoRepository = despachoRepository;
        this.paqueteRepository = paqueteRepository;
        this.reservaService = reservaService;
        this.eventBus = eventBus;
        this.props = props;
    }

    @PostConstruct
    void iniciar() {
        subscripcion = Flux.interval(props.expiryInterval())
                .flatMap(tick -> expirarVencidos()
                        .onErrorResume(e -> {
                            log.error("Fallo expirando despachos vencidos: {}", e.toString());
                            return Mono.empty();
                        }))
                .subscribe();
    }

    @PreDestroy
    void detener() {
        if (subscripcion != null) {
            subscripcion.dispose();
        }
    }

    public Mono<Void> expirarVencidos() {
        return despachoRepository.findByEstadoAndExpiraEnBefore(EstadoPaquete.ASIGNADO.name(), Instant.now())
                .concatMap(this::expirarUno)
                .then();
    }

    private Mono<Despacho> expirarUno(Despacho despacho) {
        return paqueteRepository.findByDespachoId(despacho.getId())
                .concatMap((Paquete p) -> reservaService.liberar(p.getVehiculoId(), p.getPesoKg()))
                .then(Mono.defer(() -> {
                    despacho.setEstado(EstadoPaquete.EXPIRADO.name());
                    despacho.setExpiraEn(null);
                    return despachoRepository.save(despacho);
                }))
                .doOnSuccess(d -> eventBus.publicar(EventoDespacho.de(d.getId(), d.getEstado(),
                        "Reserva expirada, cupo liberado", d.getTrazaId())));
    }
}
