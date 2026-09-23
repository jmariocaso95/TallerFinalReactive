package com.example.taller.service;

import com.example.taller.dto.ResultadoCarga;
import com.example.taller.model.Vehiculo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class VehiculoBulkService {

    private static final Logger log = LoggerFactory.getLogger(VehiculoBulkService.class);
    private static final String UPSERT = """
                INSERT INTO vehiculo (placa, ciudad, cupo_kg, reservado_kg)
                VALUES (:placa, :ciudad, :cupo_kg, :reservado_kg)
                ON CONFLICT (placa) DO UPDATE SET
                    ciudad = EXCLUDED.ciudad,
                    cupo_kg = EXCLUDED.cupo_kg,
                    reservado_kg = EXCLUDED.reservado_kg
                """;

    private final DatabaseClient db;
    private final TransactionalOperator tx;

    @Autowired
    public VehiculoBulkService(DatabaseClient db, TransactionalOperator tx) {
        this.db = db;
        this.tx = tx;
    }

    /** Entrada en streaming, lotes de 500, máximo 2 lotes concurrentes. */
    public Mono<ResultadoCarga> cargar(Flux<Vehiculo> vehiculos) {
        return vehiculos
                .buffer(500)
                .flatMap(lote -> guardarLote(lote)
                        .map(n -> new ResultadoCarga(n, 0))
                        .onErrorResume(e -> {
                            log.error("Lote de {} vehiculos falló: {}", lote.size(), e.toString());
                            return Mono.just(new ResultadoCarga(0, lote.size()));
                        }), 2)
                .reduce(new ResultadoCarga(0, 0), ResultadoCarga::mas);
    }

    private Mono<Integer> guardarLote(List<Vehiculo> lote) {
        return Flux.fromIterable(lote)
                .concatMap(p -> db.sql(UPSERT)
                        .bind("placa", p.getPlaca())
                        .bind("ciudad", p.getCiudad())
                        .bind("cupo_kg", p.getCupoKg())
                        .bind("reservado_kg", p.getReservadoKg())
                        .fetch().rowsUpdated())
                .reduce(0L, Long::sum)
                .map(Long::intValue)
                .as(tx::transactional);
    }
}

