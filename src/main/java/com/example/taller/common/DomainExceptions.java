package com.example.taller.common;

import org.springframework.http.HttpStatus;

public final class DomainExceptions {

    private DomainExceptions() {}

    public static class ValidacionException extends DomainException {
        public ValidacionException(String msg) { super(HttpStatus.BAD_REQUEST, msg); }
    }

    public static class VehiculoNoExisteException extends DomainException {
        public VehiculoNoExisteException(Long id) { super(HttpStatus.NOT_FOUND, "Vehiculo no existe: " + id); }
    }

    public static class CupoInsuficienteException extends DomainException {
        public CupoInsuficienteException(Long id, double pesoKg) {
            super(HttpStatus.CONFLICT, "Cupo insuficiente para vehiculo " + id + " (peso " + pesoKg + " kg)");
        }
    }

    public static class ZonaRiesgosaException extends DomainException {
        public ZonaRiesgosaException(int score) { super(HttpStatus.UNPROCESSABLE_ENTITY, "Zona riesgosa: " + score); }
    }

    public static class DespachoNoExisteException extends DomainException {
        public DespachoNoExisteException(Long id) { super(HttpStatus.NOT_FOUND, "Despacho no existe: " + id); }
    }

    public static class PaqueteNoExisteException extends DomainException {
        public PaqueteNoExisteException(Long id) { super(HttpStatus.NOT_FOUND, "Paquete no existe: " + id); }
    }

    public static class EstadoInvalidoException extends DomainException {
        public EstadoInvalidoException(String msg) { super(HttpStatus.CONFLICT, msg); }
    }
}
