package com.farmapredict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sismed_precios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codigoCum", "periodo", "canal"}))
public class SismedPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String codigoCum;

    @Column(nullable = false, length = 20)
    private String periodo; // ej. 2026-T3

    @Column(nullable = false, length = 10)
    private String canal; // INS (institucional) o COM (comercial)

    private Double precioMin;
    private Double precioMax;
    private Double precioProm;
    private Long unidades;

    public SismedPrecio() {}

    public Long getId() { return id; }
    public String getCodigoCum() { return codigoCum; }
    public void setCodigoCum(String codigoCum) { this.codigoCum = codigoCum; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public Double getPrecioMin() { return precioMin; }
    public void setPrecioMin(Double precioMin) { this.precioMin = precioMin; }
    public Double getPrecioMax() { return precioMax; }
    public void setPrecioMax(Double precioMax) { this.precioMax = precioMax; }
    public Double getPrecioProm() { return precioProm; }
    public void setPrecioProm(Double precioProm) { this.precioProm = precioProm; }
    public Long getUnidades() { return unidades; }
    public void setUnidades(Long unidades) { this.unidades = unidades; }
}
