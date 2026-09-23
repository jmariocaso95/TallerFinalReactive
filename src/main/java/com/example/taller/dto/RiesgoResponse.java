package com.example.taller.dto;

/** Respuesta del simulador de scoring de zona de riesgo. */
public record RiesgoResponse(String ciudad, int score) {
}
