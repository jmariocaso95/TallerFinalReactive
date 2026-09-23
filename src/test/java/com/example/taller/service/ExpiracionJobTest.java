package com.example.taller.service;

import com.example.taller.common.AppProperties;
import com.example.taller.dto.EventoDespacho;
import com.example.taller.model.Despacho;
import com.example.taller.model.Paquete;
import com.example.taller.model.Vehiculo;
import com.example.taller.repository.DespachoRepository;
import com.example.taller.repository.PaqueteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpiracionJobTest {

    private DespachoRepository despachoRepository;
    private PaqueteRepository paqueteRepository;
    private VehiculoReservaService reservaService;
    private EventBusService eventBus;
    private AppProperties props;
    private ExpiracionJob job;

    @BeforeEach
    void setUp() {
        despachoRepository = mock(DespachoRepository.class);
        paqueteRepository = mock(PaqueteRepository.class);
        reservaService = mock(VehiculoReservaService.class);
        eventBus = mock(EventBusService.class);
        props = new AppProperties(
                new AppProperties.External("http://localhost", Duration.ofSeconds(2), Duration.ofMillis(800)),
                Duration.ofMinutes(15), Duration.ofSeconds(30), 80, 50, 5);
        job = new ExpiracionJob(despachoRepository, paqueteRepository, reservaService, eventBus, props);
    }

    @Test
    @DisplayName("expirarVencidos libera el cupo de cada paquete y marca EXPIRADO")
    void expirarVencidos_liberaCupoYMarcaExpirado() {
        Despacho vencido = new Despacho(1L, "BOG", "ASIGNADO", 50000.0, 20, 50000.0, "traza-1", null,
                Instant.now(), Instant.now().minusSeconds(60));
        vencido.setId(1L);
        Paquete paquete = new Paquete(1L, 9L, 10, 100.0, "caja");

        when(despachoRepository.findByEstadoAndExpiraEnBefore(any(), any())).thenReturn(Flux.just(vencido));
        when(paqueteRepository.findByDespachoId(1L)).thenReturn(Flux.just(paquete));
        when(reservaService.liberar(9L, 10.0)).thenReturn(Mono.empty());
        when(despachoRepository.save(any(Despacho.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(job.expirarVencidos()).verifyComplete();

        verify(reservaService, times(1)).liberar(9L, 10.0);
        assertThat(vencido.getEstado()).isEqualTo("EXPIRADO");
        assertThat(vencido.getExpiraEn()).isNull();
        verify(eventBus, times(1)).publicar(any(EventoDespacho.class));
    }
}
