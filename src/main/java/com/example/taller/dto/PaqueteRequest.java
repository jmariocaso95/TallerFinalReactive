package com.example.taller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Ítem de paquete dentro de una solicitud de despacho. */
public record PaqueteRequest(
        @NotNull(message = "El peso es obligatorio") @Positive(message = "El peso debe ser mayor a cero") Integer pesoKg,
        @PositiveOrZero(message = "El valor no puede ser negativo") Double valor,
        String descripcion) {
}
