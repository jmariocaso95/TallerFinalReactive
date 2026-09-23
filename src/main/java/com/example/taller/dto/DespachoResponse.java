package com.example.taller.dto;

import com.example.taller.model.Despacho;
import com.example.taller.model.Paquete;

import java.time.Instant;
import java.util.List;

/** Vista de un despacho junto con los paquetes que transporta. */
public record DespachoResponse(
        Long id,
        Long clienteId,
        String ciudad,
        String estado,
        Double tarifa,
        Double total,
        Integer scoreRiesgo,
        String trazaId,
        Instant creadoEn,
        Instant expiraEn,
        List<Paquete> paquetes) {

    public static DespachoResponse de(Despacho d, List<Paquete> paquetes) {
        return new DespachoResponse(d.getId(), d.getClienteId(), d.getCiudad(), d.getEstado(), d.getTarifa(),
                d.getTotal(), d.getScoreRiesgo(), d.getTrazaId(), d.getCreadoEn(), d.getExpiraEn(), paquetes);
    }
}
