package com.example.taller.external;

import com.example.taller.dto.SimuladorConfig;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Estado mutable y compartido que controla el comportamiento de los servicios simulados bajo /external/**.
 * Se administra desde /external/simulator (GET/PUT/DELETE).
 */
public class SimuladorState {

    private final AtomicReference<SimuladorConfig> config = new AtomicReference<>(SimuladorConfig.porDefecto());

    public SimuladorConfig get() {
        return config.get();
    }

    public SimuladorConfig set(SimuladorConfig nuevo) {
        config.set(nuevo);
        return nuevo;
    }

    public SimuladorConfig reset() {
        SimuladorConfig defecto = SimuladorConfig.porDefecto();
        config.set(defecto);
        return defecto;
    }
}
