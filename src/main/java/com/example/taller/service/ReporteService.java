package com.example.taller.service;

import com.example.taller.dto.ReporteCiudad;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Agrega kilos y valor transportado por ciudad. La versión en vivo respeta backpressure (limitRate)
 * y acumula con scan(), emitiendo el total corriente de la ciudad afectada por cada paquete leído.
 */
@Service
public class ReporteService {

    private static final String SQL_DETALLE = """
            SELECT d.ciudad AS ciudad, p.peso_kg AS peso_kg, COALESCE(p.valor, 0) AS valor
            FROM despacho d JOIN paquete p ON p.despacho_id = d.id
            ORDER BY d.ciudad
            """;

    private static final String SQL_RESUMEN = """
            SELECT d.ciudad AS ciudad,
                   COALESCE(SUM(p.peso_kg), 0) AS total_kg,
                   COALESCE(SUM(p.valor), 0) AS total_valor,
                   COUNT(DISTINCT d.id) AS despachos
            FROM despacho d JOIN paquete p ON p.despacho_id = d.id
            GROUP BY d.ciudad
            ORDER BY d.ciudad
            """;

    private record Fila(String ciudad, int pesoKg, double valor) {}

    private record Acumulado(Map<String, ReporteCiudad> porCiudad, ReporteCiudad ultimo) {}

    private final DatabaseClient db;

    public ReporteService(DatabaseClient db) {
        this.db = db;
    }

    /** Totales finales agrupados por ciudad. */
    public Flux<ReporteCiudad> ciudades() {
        return db.sql(SQL_RESUMEN)
                .map((row, meta) -> new ReporteCiudad(
                        row.get("ciudad", String.class),
                        row.get("total_kg", Long.class),
                        row.get("total_valor", Double.class),
                        row.get("despachos", Long.class)))
                .all();
    }

    /** Acumulado en vivo, paquete a paquete, respetando backpressure con limitRate. */
    public Flux<ReporteCiudad> ciudadesEnVivo() {
        Flux<Fila> filas = db.sql(SQL_DETALLE)
                .map((row, meta) -> new Fila(
                        row.get("ciudad", String.class),
                        row.get("peso_kg", Integer.class),
                        row.get("valor", Double.class)))
                .all();

        return filas.limitRate(50)
                .scan(new Acumulado(new HashMap<>(), null), (acc, fila) -> {
                    Map<String, ReporteCiudad> copia = new HashMap<>(acc.porCiudad());
                    ReporteCiudad actual = copia.getOrDefault(fila.ciudad(), ReporteCiudad.vacio(fila.ciudad()));
                    ReporteCiudad actualizado = actual.sumar(fila.pesoKg(), fila.valor());
                    copia.put(fila.ciudad(), actualizado);
                    return new Acumulado(copia, actualizado);
                })
                .skip(1)
                .map(Acumulado::ultimo);
    }
}
