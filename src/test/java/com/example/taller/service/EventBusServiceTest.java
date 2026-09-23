package com.example.taller.service;

import com.example.taller.dto.EventoDespacho;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EventBusServiceTest {

    @Test
    @DisplayName("tablero recibe todos los eventos publicados")
    void tablero_recibeTodosLosEventos() {
        EventBusService bus = new EventBusService();
        EventoDespacho evento = EventoDespacho.de(1L, "ASIGNADO", "msg", "traza-1");

        StepVerifier.create(bus.tablero().take(1))
                .then(() -> bus.publicar(evento))
                .expectNext(evento)
                .verifyComplete();
    }

    @Test
    @DisplayName("porDespacho filtra por id y se cierra al llegar a un estado terminal")
    void porDespacho_filtraYCierraEnEstadoTerminal() {
        EventBusService bus = new EventBusService();
        EventoDespacho otroDespacho = EventoDespacho.de(2L, "ASIGNADO", "otro", "traza-2");
        EventoDespacho enCurso = EventoDespacho.de(1L, "ASIGNADO", "en curso", "traza-1");
        EventoDespacho terminal = EventoDespacho.de(1L, "ENTREGADO", "entregado", "traza-1");
        EventoDespacho despuesDeTerminal = EventoDespacho.de(1L, "EN_RUTA", "no debería llegar", "traza-1");

        StepVerifier.create(bus.porDespacho(1L))
                .then(() -> {
                    bus.publicar(otroDespacho);
                    bus.publicar(enCurso);
                    bus.publicar(terminal);
                    bus.publicar(despuesDeTerminal);
                })
                .expectNext(enCurso)
                .expectNext(terminal)
                .verifyComplete();
    }
}
