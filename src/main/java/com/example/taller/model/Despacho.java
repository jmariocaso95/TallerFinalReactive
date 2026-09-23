package com.example.taller.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("despacho")
public class Despacho {
    @Id
    private Long id;
    private Long clienteId;
    private String ciudad;
    private String estado;
    private Double tarifa;
    private Double total;
    private Integer scoreRiesgo;
    private String trazaId;
    private String idemKey;
    private Instant creadoEn;
    private Instant expiraEn;

    public Despacho() {}

    public Despacho(Long clienteId, String ciudad, String estado, Double tarifa, Integer scoreRiesgo, Double total, String trazaId, String idemKey, Instant creadoEn, Instant expiraEn) {
        this.clienteId = clienteId;
        this.ciudad = ciudad;
        this.estado = estado;
        this.tarifa = tarifa;
        this.scoreRiesgo = scoreRiesgo;
        this.total = total;
        this.trazaId = trazaId;
        this.idemKey = idemKey;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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

    public Double getTarifa() {
        return tarifa;
    }

    public void setTarifa(Double tarifa) {
        this.tarifa = tarifa;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Integer getScoreRiesgo() {
        return scoreRiesgo;
    }

    public void setScoreRiesgo(Integer scoreRiesgo) {
        this.scoreRiesgo = scoreRiesgo;
    }

    public String getTrazaId() {
        return trazaId;
    }

    public void setTrazaId(String trazaId) {
        this.trazaId = trazaId;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(Instant expiraEn) {
        this.expiraEn = expiraEn;
    }
}
