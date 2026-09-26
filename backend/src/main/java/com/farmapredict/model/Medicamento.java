package com.farmapredict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicamentos")
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 300)
    private String nombre;

    @Column(length = 100)
    private String concentracion;

    @Column(length = 50)
    private String categoria; // UCI, CRONICO, ANTIBIOTICO, etc.

    public Medicamento() {}

    public Medicamento(String codigo, String nombre, String concentracion, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.concentracion = concentracion;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
