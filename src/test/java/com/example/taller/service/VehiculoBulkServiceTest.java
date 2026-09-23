package com.example.taller.service;

import com.example.taller.dto.ResultadoCarga;
import com.example.taller.model.Vehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VehiculoBulkServiceTest {

    private DatabaseClient db;
    private TransactionalOperator tx;
    private VehiculoBulkService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        db = mock(DatabaseClient.class);
        tx = mock(TransactionalOperator.class);
        service = new VehiculoBulkService(db, tx);

        DatabaseClient.GenericExecuteSpec execSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        FetchSpec<Map<String, Object>> fetchSpec = mock(FetchSpec.class);

        when(db.sql(anyString())).thenReturn(execSpec);
        when(execSpec.bind(anyString(), any())).thenReturn(execSpec);
        when(execSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        // El TransactionalOperator simplemente ejecuta el Mono recibido, sin transacción real.
        when(tx.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("cargar procesa el lote y reporta guardados")
    void cargar_procesaLoteYReportaGuardados() {
        Vehiculo v1 = new Vehiculo(null, "AAA111", 100.0, 0.0, "BOG", "ACTIVO");
        Vehiculo v2 = new Vehiculo(null, "BBB222", 200.0, 0.0, "MDE", "ACTIVO");

        Mono<ResultadoCarga> resultado = service.cargar(Flux.just(v1, v2));

        StepVerifier.create(resultado)
                .assertNext(r -> {
                    assertThat(r.guardados()).isEqualTo(2);
                    assertThat(r.fallidos()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("cargar con flujo vacío reporta cero guardados y cero fallidos")
    void cargar_flujoVacio_reportaCeros() {
        StepVerifier.create(service.cargar(Flux.empty()))
                .assertNext(r -> {
                    assertThat(r.guardados()).isEqualTo(0);
                    assertThat(r.fallidos()).isEqualTo(0);
                })
                .verifyComplete();
    }
}
