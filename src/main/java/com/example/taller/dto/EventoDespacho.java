package com.example.taller.dto;

import java.time.Instant;

/** Evento emitido al bus interno cada vez que un despacho cambia de estado. */
public record EventoDespacho(Long despachoId, String estado, String mensaje, String trazaId, Instant instante) {

    public static EventoDespacho de(Long despachoId, String estado, String mensaje, String trazaId) {
        return new EventoDespacho(despachoId, estado, mensaje, trazaId, Instant.now());
    }
}
