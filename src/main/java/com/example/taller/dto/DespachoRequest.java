package com.example.taller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Solicitud de despacho: cliente, ciudad destino y los paquetes a transportar. */
public record DespachoRequest(
        @NotNull(message = "El cliente es obligatorio") Long clienteId,
        @NotBlank(message = "La ciudad es obligatoria") String ciudad,
        @NotEmpty(message = "Debe incluir al menos un paquete") @Valid List<PaqueteRequest> paquetes) {
}
