package com.example.taller.controller;

import com.example.taller.dto.ReporteCiudad;
import com.example.taller.service.ReporteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/** Reportes agregados de kilos y valor transportado por ciudad. */
@Tag(name = "Reportes", description = "Totales por ciudad, en bloque o acumulados en vivo")
@RestController
@RequestMapping("/api/reports")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping(value = "/ciudades", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ReporteCiudad> ciudades() {
        return reporteService.ciudades();
    }

    @GetMapping(value = "/ciudades/stream", produces = "application/x-ndjson")
    public Flux<ReporteCiudad> ciudadesStream() {
        return reporteService.ciudadesEnVivo();
    }
}
