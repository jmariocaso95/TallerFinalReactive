package com.example.taller.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehiculo")
public class Vehiculo {
    @Id
    private Long id;
    private String placa;
    private Double cupoKg;
    private Double reservadoKg;
    private String ciudad;
    private String estado;

    public Vehiculo() {}

    public Vehiculo(Long id, String placa, Double cupoKg, Double reservadoKg, String ciudad, String estado) {
        this.id = id;
        this.placa = placa;
        this.cupoKg = cupoKg;
        this.reservadoKg = reservadoKg;
        this.ciudad = ciudad;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Double getCupoKg() {
        return cupoKg;
    }

    public void setCupoKg(Double cupoKg) {
        this.cupoKg = cupoKg;
    }

    public Double getReservadoKg() {
        return reservadoKg;
    }

    public void setReservadoKg(Double reservadoKg) {
        this.reservadoKg = reservadoKg;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
