package com.example.taller.service;

import com.example.taller.common.AppProperties;
import com.example.taller.common.DomainExceptions.CupoInsuficienteException;
import com.example.taller.common.DomainExceptions.DespachoNoExisteException;
import com.example.taller.common.DomainExceptions.EstadoInvalidoException;
import com.example.taller.common.DomainExceptions.ZonaRiesgosaException;
import com.example.taller.dto.ClimaResponse;
import com.example.taller.dto.DespachoRequest;
import com.example.taller.dto.PaqueteRequest;
import com.example.taller.external.ClimaClient;
import com.example.taller.external.PricingClient;
import com.example.taller.external.RiesgoClient;
import com.example.taller.model.Despacho;
import com.example.taller.model.Paquete;
import com.example.taller.model.Vehiculo;
import com.example.taller.repository.DespachoRepository;
import com.example.taller.repository.PaqueteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DespachoServiceTest {

    private DespachoRepository despachoRepository;
    private PaqueteRepository paqueteRepository;
    private VehiculoReservaService reservaService;
    private PricingClient pricingClient;
    private ClimaClient climaClient;
    private RiesgoClient riesgoClient;
    private EventBusService eventBus;
    private TransactionalOperator tx;
    private AppProperties props;
    private DespachoService despachoService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        despachoRepository = mock(DespachoRepository.class);
        paqueteRepository = mock(PaqueteRepository.class);
        reservaService = mock(VehiculoReservaService.class);
        pricingClient = mock(PricingClient.class);
        climaClient = mock(ClimaClient.class);
        riesgoClient = mock(RiesgoClient.class);
        eventBus = mock(EventBusService.class);
        tx = mock(TransactionalOperator.class);
        props = new AppProperties(
                new AppProperties.External("http://localhost", java.time.Duration.ofSeconds(2), java.time.Duration.ofMillis(800)),
                java.time.Duration.ofMinutes(15),
                java.time.Duration.ofSeconds(30),
                80, 50, 5);

        when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

        despachoService = new DespachoService(despachoRepository, paqueteRepository, reservaService,
                pricingClient, climaClient, riesgoClient, eventBus, tx, props);
    }

    private Despacho despachoGuardado(Long id, String estado) {
        Despacho d = new Despacho(1L, "BOG", estado, null, null, null, "traza-1", null, Instant.now(), null);
        d.setId(id);
        return d;
    }

    private DespachoRequest requestUnPaquete() {
        return new DespachoRequest(1L, "BOG", List.of(new PaqueteRequest(10, 100.0, "caja")));
    }

    @Test
    @DisplayName("crear asigna el despacho cuando el riesgo es bajo")
    void crear_asignaDespachoConRiesgoBajo() {
        Despacho recibido = despachoGuardado(1L, "RECIBIDO");
        Vehiculo vehiculo = new Vehiculo(9L, "ABC123", 90.0, 10.0, "BOG", "ACTIVO");
        VehiculoReservaService.ReservaHecha reserva = new VehiculoReservaService.ReservaHecha(vehiculo, 10.0);

        when(despachoRepository.save(any(Despacho.class))).thenAnswer(inv -> {
            Despacho arg = inv.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(1L);
            }
            return Mono.just(arg);
        });
        when(reservaService.reservarParaPaquetes(eq("BOG"), any())).thenReturn(Mono.just(List.of(reserva)));
        when(pricingClient.tarifaPara("BOG")).thenReturn(Mono.just(55000.0));
        when(climaClient.climaPara("BOG")).thenReturn(Mono.just(new ClimaResponse("BOG", "despejado", true)));
        when(riesgoClient.scorePara("BOG")).thenReturn(Mono.just(20));
        when(paqueteRepository.saveAll(any(List.class))).thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));

        StepVerifier.create(despachoService.crear(requestUnPaquete(), null))
                .assertNext(res -> {
                    assertThat(res.estado()).isEqualTo("ASIGNADO");
                    assertThat(res.tarifa()).isEqualTo(55000.0);
                    assertThat(res.paquetes()).hasSize(1);
                })
                .verifyComplete();

        verify(reservaService, never()).compensar(any());
    }

    @Test
    @DisplayName("crear propaga CupoInsuficienteException cuando no hay cupo")
    void crear_propagaCupoInsuficiente() {
        when(despachoRepository.save(any(Despacho.class))).thenAnswer(inv -> {
            Despacho arg = inv.getArgument(0);
            arg.setId(1L);
            return Mono.just(arg);
        });
        when(reservaService.reservarParaPaquetes(eq("BOG"), any()))
                .thenReturn(Mono.error(new CupoInsuficienteException(9L, 10.0)));

        StepVerifier.create(despachoService.crear(requestUnPaquete(), null))
                .expectError(CupoInsuficienteException.class)
                .verify();
    }

    @Test
    @DisplayName("crear rechaza con ZonaRiesgosaException y compensa el cupo cuando el score supera el umbral")
    void crear_rechazaPorZonaRiesgosa() {
        Vehiculo vehiculo = new Vehiculo(9L, "ABC123", 90.0, 10.0, "BOG", "ACTIVO");
        VehiculoReservaService.ReservaHecha reserva = new VehiculoReservaService.ReservaHecha(vehiculo, 10.0);

        when(despachoRepository.save(any(Despacho.class))).thenAnswer(inv -> {
            Despacho arg = inv.getArgument(0);
            if (arg.getId() == null) {
                arg.setId(1L);
            }
            return Mono.just(arg);
        });
        when(reservaService.reservarParaPaquetes(eq("BOG"), any())).thenReturn(Mono.just(List.of(reserva)));
        when(reservaService.compensar(any())).thenReturn(Mono.empty());
        when(pricingClient.tarifaPara("BOG")).thenReturn(Mono.just(55000.0));
        when(climaClient.climaPara("BOG")).thenReturn(Mono.just(new ClimaResponse("BOG", "despejado", true)));
        when(riesgoClient.scorePara("BOG")).thenReturn(Mono.just(95));

        StepVerifier.create(despachoService.crear(requestUnPaquete(), null))
                .expectError(ZonaRiesgosaException.class)
                .verify();

        verify(reservaService, times(1)).compensar(List.of(reserva));
    }

    @Test
    @DisplayName("crear con Idempotency-Key existente devuelve el despacho ya creado sin repetir el saga")
    void crear_esIdempotente() {
        Despacho existente = despachoGuardado(1L, "ASIGNADO");
        when(despachoRepository.findByIdemKey("idem-1")).thenReturn(Mono.just(existente));
        when(despachoRepository.findById(1L)).thenReturn(Mono.just(existente));
        when(paqueteRepository.findByDespachoId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(despachoService.crear(requestUnPaquete(), "idem-1"))
                .assertNext(res -> assertThat(res.id()).isEqualTo(1L))
                .verifyComplete();

        verify(reservaService, never()).reservarParaPaquetes(anyString(), any());
    }

    @Test
    @DisplayName("obtener devuelve DespachoNoExisteException si no existe")
    void obtener_noExiste() {
        when(despachoRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(despachoService.obtener(99L))
                .expectError(DespachoNoExisteException.class)
                .verify();
    }

    @Test
    @DisplayName("confirmar cambia de ASIGNADO a EN_RUTA y confirma el cupo de cada paquete")
    void confirmar_pasaAEnRuta() {
        Despacho asignado = despachoGuardado(1L, "ASIGNADO");
        Paquete paquete = new Paquete(1L, 9L, 10, 100.0, "caja");

        when(despachoRepository.findById(1L)).thenReturn(Mono.just(asignado));
        when(paqueteRepository.findByDespachoId(1L)).thenReturn(Flux.just(paquete));
        when(reservaService.confirmar(9L, 10.0)).thenReturn(Mono.empty());
        when(despachoRepository.save(any(Despacho.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(despachoService.confirmar(1L))
                .assertNext(res -> assertThat(res.estado()).isEqualTo("EN_RUTA"))
                .verifyComplete();

        verify(reservaService, times(1)).confirmar(9L, 10.0);
    }

    @Test
    @DisplayName("confirmar falla con EstadoInvalidoException si el despacho no está ASIGNADO")
    void confirmar_fallaSiNoEstaAsignado() {
        Despacho enRuta = despachoGuardado(1L, "EN_RUTA");
        when(despachoRepository.findById(1L)).thenReturn(Mono.just(enRuta));

        StepVerifier.create(despachoService.confirmar(1L))
                .expectError(EstadoInvalidoException.class)
                .verify();
    }
}
