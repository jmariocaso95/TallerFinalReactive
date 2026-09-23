package com.example.taller.controller;

import com.example.taller.dto.ReporteCiudad;
import com.example.taller.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteControllerTest {

    private ReporteService reporteService;
    private ReporteController controller;

    @BeforeEach
    void setUp() {
        reporteService = mock(ReporteService.class);
        controller = new ReporteController(reporteService);
    }

    @Test
    @DisplayName("ciudades delega en el reporte por lote")
    void ciudades_delegaEnReporteService() {
        ReporteCiudad r = new ReporteCiudad("BOG", 100L, 5000.0, 3L);
        when(reporteService.ciudades()).thenReturn(Flux.just(r));

        StepVerifier.create(controller.ciudades())
                .expectNext(r)
                .verifyComplete();
    }

    @Test
    @DisplayName("ciudadesStream delega en el reporte en vivo")
    void ciudadesStream_delegaEnReporteEnVivo() {
        ReporteCiudad r = new ReporteCiudad("BOG", 100L, 5000.0, 3L);
        when(reporteService.ciudadesEnVivo()).thenReturn(Flux.just(r));

        StepVerifier.create(controller.ciudadesStream())
                .expectNext(r)
                .verifyComplete();
    }
}
