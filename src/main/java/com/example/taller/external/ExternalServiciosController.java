package com.example.taller.external;

import com.example.taller.dto.ClimaResponse;
import com.example.taller.dto.RiesgoResponse;
import com.example.taller.dto.TarifaResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicios externos simulados dentro de la misma app. Su comportamiento (fallos, latencia, score)
 * se controla en caliente vía {@link ExternalSimuladorController} (/external/simulator).
 */
@Tag(name = "Servicios simulados", description = "Tarifa, clima y riesgo simulados, base para el saga de despachos")
@RestController
@RequestMapping("/external")
public class ExternalServiciosController {

    private final SimuladorState state;

    public ExternalServiciosController(SimuladorState state) {
        this.state = state;
    }

    /** Falla intermitente según tarifaFalloProbabilidad, para forzar el retryWhen del cliente. */
    @GetMapping("/tarifas/{ciudad}")
    public Mono<TarifaResponse> tarifa(@PathVariable String ciudad) {
        double p = state.get().tarifaFalloProbabilidad();
        if (ThreadLocalRandom.current().nextDouble() < p) {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Tarifa no disponible"));
        }
        double tarifa = 50_000 + Math.abs(ciudad.hashCode() % 20_000);
        return Mono.just(new TarifaResponse(ciudad, tarifa));
    }

    /** Lento a propósito: simula la latencia de un servicio de clima/ventanas de entrega. */
    @GetMapping("/clima/{ciudad}")
    public Mono<ClimaResponse> clima(@PathVariable String ciudad) {
        long latenciaMs = state.get().climaLatenciaMs();
        boolean favorable = ThreadLocalRandom.current().nextInt(10) > 1;
        ClimaResponse respuesta = new ClimaResponse(ciudad, favorable ? "08:00-18:00" : "10:00-12:00", favorable);
        return Mono.just(respuesta).delayElement(Duration.ofMillis(latenciaMs));
    }

    /** Puede "colgarse" (latencia alta) para ejercitar el timeout del cliente de riesgo. */
    @GetMapping("/riesgo/{ciudad}")
    public Mono<RiesgoResponse> riesgo(@PathVariable String ciudad) {
        long latenciaMs = state.get().riesgoLatenciaMs();
        int score = state.get().riesgoScore();
        return Mono.just(new RiesgoResponse(ciudad, score)).delayElement(Duration.ofMillis(latenciaMs));
    }
}
