package com.example.taller.service;

import com.example.taller.common.EstadoPaquete;
import com.example.taller.dto.EventoDespacho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Bus interno de eventos de despacho. Un único Sinks.Many multicast alimenta dos streams SSE:
 * uno filtrado por despacho (se cierra al llegar a un estado terminal) y otro global para el tablero de ops.
 */
@Service
public class EventBusService {

    private static final Logger log = LoggerFactory.getLogger(EventBusService.class);

    private final Sinks.Many<EventoDespacho> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publicar(EventoDespacho evento) {
        Sinks.EmitResult resultado = sink.tryEmitNext(evento);
        if (resultado.isFailure()) {
            log.warn("No se pudo emitir evento {}: {}", evento, resultado);
        }
    }

    /** Stream global compartido (hot): todos los suscriptores ven los mismos eventos desde que se conectan. */
    public Flux<EventoDespacho> tablero() {
        return sink.asFlux();
    }

    /** Stream de un despacho puntual; se cierra solo cuando ese despacho llega a un estado terminal. */
    public Flux<EventoDespacho> porDespacho(Long despachoId) {
        return sink.asFlux()
                .filter(e -> e.despachoId().equals(despachoId))
                .takeUntil(e -> EstadoPaquete.valueOf(e.estado()).esTerminal());
    }
}
