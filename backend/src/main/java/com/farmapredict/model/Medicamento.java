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

    @Column(nullable = false, length = 500)
    private String nombre;

    @Column(length = 200)
    private String concentracion;

    @Column(length = 50)
    private String categoria; // UCI, CRONICO, ANTIBIOTICO, etc.

    @Column(length = 1000)
    private String principioActivo;

    @Column(length = 500)
    private String titular;

    @Column(length = 20)
    private String estadoRegistro = "VIGENTE"; // VIGENTE, VENCIDO, RENOVACION, OTRO

    @Column(length = 200)
    private String registroSanitario;

    public Medicamento() {}

    public Medicamento(String codigo, String nombre, String concentracion, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.concentracion = concentracion;
        this.categoria = categoria;
    }

    public Medicamento(String codigo, String nombre, String concentracion, String categoria,
                       String principioActivo, String titular, String estadoRegistro, String registroSanitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.concentracion = concentracion;
        this.categoria = categoria;
        this.principioActivo = principioActivo;
        this.titular = titular;
        this.estadoRegistro = estadoRegistro;
        this.registroSanitario = registroSanitario;
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
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public String getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(String estadoRegistro) { this.estadoRegistro = estadoRegistro; }
    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String registroSanitario) { this.registroSanitario = registroSanitario; }
}
