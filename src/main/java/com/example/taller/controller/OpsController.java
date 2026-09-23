package com.example.taller.controller;

import com.example.taller.dto.EventoDespacho;
import com.example.taller.service.EventBusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/** Tablero operativo: un único stream SSE compartido con todos los eventos de todos los despachos. */
@Tag(name = "Operaciones", description = "Tablero en vivo compartido entre todos los observadores")
@RestController
@RequestMapping("/api/ops")
public class OpsController {

    private final EventBusService eventBus;

    public OpsController(EventBusService eventBus) {
        this.eventBus = eventBus;
    }

    @GetMapping(value = "/tablero", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EventoDespacho> tablero() {
        return eventBus.tablero();
    }
}
