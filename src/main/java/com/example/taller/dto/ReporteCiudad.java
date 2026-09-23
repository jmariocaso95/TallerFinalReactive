package com.example.taller.dto;

/** Totales acumulados de kilos y valor transportado por ciudad. */
public record ReporteCiudad(String ciudad, long totalKg, double totalValor, long despachos) {

    public ReporteCiudad sumar(long kg, double valor) {
        return new ReporteCiudad(ciudad, this.totalKg + kg, this.totalValor + valor, this.despachos + 1);
    }

    public static ReporteCiudad vacio(String ciudad) {
        return new ReporteCiudad(ciudad, 0L, 0.0, 0L);
    }
}
