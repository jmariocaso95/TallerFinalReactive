package com.example.taller.controller;

import com.example.taller.dto.DespachoRequest;
import com.example.taller.dto.DespachoResponse;
import com.example.taller.dto.EventoDespacho;
import com.example.taller.service.DespachoService;
import com.example.taller.service.EventBusService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Despacho", description = "API para gestionar los despachos de paquetes")
@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    private final DespachoService despachoService;
    private final EventBusService eventBus;

    public DespachoController(DespachoService despachoService, EventBusService eventBus) {
        this.despachoService = despachoService;
        this.eventBus = eventBus;
    }

    @PostMapping
    public Mono<ResponseEntity<DespachoResponse>> crear(
            @Valid @RequestBody DespachoRequest request,
            @Parameter(description = "Evita crear el mismo despacho dos veces si se reintenta la solicitud")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return despachoService.crear(request, idempotencyKey)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @GetMapping("/{id}")
    public Mono<DespachoResponse> obtener(@PathVariable Long id) {
        return despachoService.obtener(id);
    }

    @PostMapping("/{id}/confirm")
    public Mono<DespachoResponse> confirmar(@PathVariable Long id) {
        return despachoService.confirmar(id);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EventoDespacho> eventos(@PathVariable Long id) {
        return eventBus.porDespacho(id);
    }
}
