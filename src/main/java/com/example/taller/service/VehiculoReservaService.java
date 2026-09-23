package com.example.taller.service;

import com.example.taller.common.DomainExceptions.CupoInsuficienteException;
import com.example.taller.model.Vehiculo;
import com.example.taller.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Reserva y libera cupo de vehículos. La reserva de un cupo individual es atómica en base de datos
 * (UPDATE ... WHERE cupo_kg >= :peso RETURNING *); reservarParaPaquetes encadena varias reservas y,
 * si alguna falla a mitad de camino, compensa (saga) devolviendo el cupo ya tomado.
 */
@Service
public class VehiculoReservaService {

    /** Reserva ya concretada: vehículo resultante y peso que se le descontó, para poder compensarla luego. */
    public record ReservaHecha(Vehiculo vehiculo, double pesoKg) {}

    private final VehiculoRepository vehiculoRepository;

    public VehiculoReservaService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    /** Intenta reservar el peso indicado en el primer vehículo ACTIVO de la ciudad con cupo suficiente. */
    public Mono<Vehiculo> reservarUno(String ciudad, double pesoKg) {
        return vehiculoRepository.findByCiudadAndEstadoOrderByCupoKgDesc(ciudad, "ACTIVO")
                .collectList()
                .flatMap(candidatos -> intentar(candidatos.iterator(), pesoKg));
    }

    private Mono<Vehiculo> intentar(Iterator<Vehiculo> candidatos, double pesoKg) {
        if (!candidatos.hasNext()) {
            return Mono.error(new CupoInsuficienteException(0L, pesoKg));
        }
        Vehiculo candidato = candidatos.next();
        return vehiculoRepository.reservarCupo(candidato.getId(), pesoKg)
                .switchIfEmpty(Mono.defer(() -> intentar(candidatos, pesoKg)));
    }

    /**
     * Reserva un cupo por cada peso de la lista (en orden), uno a la vez. Si alguno falla,
     * libera (compensa) todo lo ya reservado en esta llamada y propaga el error original.
     */
    public Mono<List<ReservaHecha>> reservarParaPaquetes(String ciudad, List<Integer> pesosKg) {
        List<ReservaHecha> reservadas = Collections.synchronizedList(new ArrayList<>());
        return Flux.fromIterable(pesosKg)
                .concatMap(peso -> reservarUno(ciudad, peso)
                        .doOnNext(v -> reservadas.add(new ReservaHecha(v, peso))))
                .then(Mono.defer(() -> Mono.just(List.copyOf(reservadas))))
                .onErrorResume(err -> compensar(reservadas).then(Mono.error(err)));
    }

    /** Libera el cupo de una reserva puntual. */
    public Mono<Void> liberar(Long vehiculoId, double pesoKg) {
        return vehiculoRepository.liberarCupo(vehiculoId, pesoKg).then();
    }

    /** Libera el cupo de todas las reservas indicadas (saga de compensación). */
    public Mono<Void> compensar(List<ReservaHecha> reservadas) {
        return Flux.fromIterable(List.copyOf(reservadas))
                .concatMap(r -> liberar(r.vehiculo().getId(), r.pesoKg()))
                .then();
    }

    /** Confirmación: el cupo reservado pasa a consumido definitivamente. */
    public Mono<Void> confirmar(Long vehiculoId, double pesoKg) {
        return vehiculoRepository.confirmarCupo(vehiculoId, pesoKg).then();
    }
}
