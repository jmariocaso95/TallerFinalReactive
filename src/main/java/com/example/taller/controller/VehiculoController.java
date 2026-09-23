package com.example.taller.controller;

import com.example.taller.dto.ResultadoCarga;
import com.example.taller.model.Vehiculo;
import com.example.taller.repository.VehiculoRepository;
import com.example.taller.service.VehiculoBulkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Tag(name = "Vehiculo", description = "API para gestionar vehículos")
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {
    private final VehiculoRepository vehiculoRepository;
    private final VehiculoBulkService service;

    @Autowired
    public VehiculoController(VehiculoBulkService service, VehiculoRepository vehiculoRepository) {
        this.service = service;
        this.vehiculoRepository = vehiculoRepository;

    }



    @GetMapping
    public Flux<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Vehiculo>> obtenerPorId(@PathVariable Long id) {
        return vehiculoRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/placa/{placa}")
    public Mono<ResponseEntity<Vehiculo>> obtenerPorPlaca(@PathVariable String placa) {
        return vehiculoRepository.findByPlaca(placa)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/ciudad/{ciudad}")
    public Flux<Vehiculo> obtenerPorCiudad(@PathVariable String ciudad) {
        return vehiculoRepository.findByCiudad(ciudad);
    }

    @GetMapping("/estado/{estado}")
    public Flux<Vehiculo> obtenerPorEstado(@PathVariable String estado) {
        return vehiculoRepository.findByEstado(estado);
    }

    @PostMapping
    public Mono<Vehiculo> crearNuevo(@RequestBody Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Vehiculo> obtenerTodosStream() {
        return vehiculoRepository.findAll().delayElements(Duration.ofSeconds(1));
    }


    @PutMapping("/{id}")
    public Mono<ResponseEntity<Vehiculo>> actualizar(@PathVariable Long id, @RequestBody Vehiculo vehiculoActualizado) {
        vehiculoActualizado.setId(id);  // Asegúrate de establecer el ID
        return vehiculoRepository.findById(id)
                .flatMap(v -> {
                    v.setPlaca(vehiculoActualizado.getPlaca());
                    v.setCupoKg(vehiculoActualizado.getCupoKg());
                    v.setReservadoKg(vehiculoActualizado.getReservadoKg());
                    v.setCiudad(vehiculoActualizado.getCiudad());
                    v.setEstado(vehiculoActualizado.getEstado());
                    return vehiculoRepository.save(v);  // Ahora hará UPDATE
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable Long id) {
        return vehiculoRepository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Carga masiva de vehículos (NDJSON)",
            description = "Un JSON por línea. Se procesa en lotes de 500 con INSERT ... ON CONFLICT DO UPDATE.")
    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<ResultadoCarga> cargaMasiva(@RequestBody Flux<Vehiculo> vehiculos) {
        return service.cargar(vehiculos);
    }

}
