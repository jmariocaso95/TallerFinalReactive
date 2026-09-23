package com.example.taller.dto;

/** Respuesta del simulador de clima/ventana de entrega por ciudad. */
public record ClimaResponse(String ciudad, String ventana, boolean favorable) {
}
