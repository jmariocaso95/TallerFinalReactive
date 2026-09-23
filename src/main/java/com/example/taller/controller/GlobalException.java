package com.example.taller.controller;

import com.example.taller.common.CorrelationWebFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Traduce errores de validación (@Valid) al mismo cuerpo uniforme que el resto de errores del dominio. */
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(WebExchangeBindException ex) {
        return Mono.deferContextual(ctx -> {
            Map<String, String> errores = ex.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            err -> err.getDefaultMessage(),
                            (msg1, msg2) -> msg1,
                            LinkedHashMap::new));

            Map<String, Object> body = Map.of(
                    "codigo", "ValidacionException",
                    "mensaje", errores.isEmpty() ? "Solicitud inválida" : String.join("; ", errores.values()),
                    "detalle", errores,
                    "trazaId", ctx.getOrDefault(CorrelationWebFilter.KEY, "n/a"),
                    "instante", Instant.now().toString());

            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
        });
    }
}
