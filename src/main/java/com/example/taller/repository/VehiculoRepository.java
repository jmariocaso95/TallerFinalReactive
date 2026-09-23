package com.example.taller.repository;

import com.example.taller.model.Vehiculo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VehiculoRepository extends R2dbcRepository<Vehiculo, Long> {
    Mono<Vehiculo> findByPlaca(String placa);
    Flux<Vehiculo> findByCiudad(String ciudad);
    Flux<Vehiculo> findByEstado(String estado);
    Flux<Vehiculo> findByCiudadAndEstadoOrderByCupoKgDesc(String ciudad, String estado);

    /**
     * Reserva cupo de forma atómica: solo actualiza (y devuelve) la fila si aún hay cupo suficiente.
     * El UPDATE...RETURNING evita la carrera de lectura-luego-escritura entre solicitudes concurrentes.
     */
    @Query("UPDATE vehiculo SET cupo_kg = cupo_kg - :pesoKg, reservado_kg = reservado_kg + :pesoKg " +
            "WHERE id = :id AND cupo_kg >= :pesoKg RETURNING *")
    Mono<Vehiculo> reservarCupo(Long id, double pesoKg);

    /** Compensación de la saga: devuelve el cupo reservado (libera cupo_kg y reservado_kg). */
    @Query("UPDATE vehiculo SET cupo_kg = cupo_kg + :pesoKg, reservado_kg = reservado_kg - :pesoKg " +
            "WHERE id = :id RETURNING *")
    Mono<Vehiculo> liberarCupo(Long id, double pesoKg);

    /** Confirmación: el cupo reservado pasa a consumido definitivamente (cupo_kg ya quedó descontado). */
    @Query("UPDATE vehiculo SET reservado_kg = reservado_kg - :pesoKg WHERE id = :id RETURNING *")
    Mono<Vehiculo> confirmarCupo(Long id, double pesoKg);
}
