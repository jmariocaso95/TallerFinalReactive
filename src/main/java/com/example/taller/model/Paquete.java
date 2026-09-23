package com.example.taller.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("paquete")
public class Paquete {
    @Id
    private Long id;
    private Long despachoId;
    private Long vehiculoId;
    @NotBlank(message = "El peso no puede estar vacío")
    private Integer pesoKg;
    private Double valor;
    private String descripcion;

    public Paquete() {}

    public Paquete(Long despachoId, Long vehiculoId, Integer pesoKg, Double valor, String descripcion) {
        this.despachoId = despachoId;
        this.vehiculoId = vehiculoId;
        this.pesoKg = pesoKg;
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDespachoId() {
        return despachoId;
    }

    public void setDespachoId(Long despachoId) {
        this.despachoId = despachoId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Integer getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Integer pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
