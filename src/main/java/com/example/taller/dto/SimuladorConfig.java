package com.example.taller.dto;

/**
 * Configuración mutable del simulador de servicios externos, expuesta en /external/simulator.
 *
 * @param tarifaFalloProbabilidad probabilidad (0..1) de que /external/tarifas falle con 503
 * @param climaLatenciaMs         latencia simulada (ms) de /external/clima antes de responder
 * @param riesgoLatenciaMs        latencia simulada (ms) de /external/riesgo antes de responder (puede colgarse)
 * @param riesgoScore             score de riesgo fijo devuelto por el simulador
 */
public record SimuladorConfig(double tarifaFalloProbabilidad, long climaLatenciaMs, long riesgoLatenciaMs, int riesgoScore) {

    public static SimuladorConfig porDefecto() {
        return new SimuladorConfig(0.0, 0L, 0L, 50);
    }
}
