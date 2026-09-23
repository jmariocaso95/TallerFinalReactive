package com.example.taller.common;

public enum EstadoPaquete {
    RECIBIDO,
    ASIGNADO,
    EN_RUTA,
    ENTREGADO,
    RECHAZADO,
    EXPIRADO,
    COMPENSADO;

    public boolean esTerminal() {
        return this == ENTREGADO || this == RECHAZADO ||
                this == EXPIRADO || this == COMPENSADO;
    }
}