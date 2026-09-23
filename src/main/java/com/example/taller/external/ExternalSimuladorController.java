package com.example.taller.external;

import com.example.taller.dto.SimuladorConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/** Panel de control del simulador: ajusta fallas, latencia y riesgo que devuelven los servicios de /external/**. */
@Tag(name = "Simulador externo", description = "Controla fallos, latencia y riesgo de los servicios simulados")
@RestController
@RequestMapping("/external/simulator")
public class ExternalSimuladorController {

    private final SimuladorState state;

    public ExternalSimuladorController(SimuladorState state) {
        this.state = state;
    }

    @GetMapping
    public SimuladorConfig obtener() {
        return state.get();
    }

    @PutMapping
    public SimuladorConfig actualizar(@RequestBody SimuladorConfig nuevaConfig) {
        return state.set(nuevaConfig);
    }

    @DeleteMapping
    public SimuladorConfig reiniciar() {
        return state.reset();
    }
}
