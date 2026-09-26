package com.farmapredict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventarios", uniqueConstraints = @UniqueConstraint(columnNames = {"medicamento_id", "sede"}))
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Medicamento medicamento;

    @Column(nullable = false, length = 100)
    private String sede;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Double demandaSemanal;

    public Inventario() {}

    public Inventario(Medicamento medicamento, String sede, Integer stock, Double demandaSemanal) {
        this.medicamento = medicamento;
        this.sede = sede;
        this.stock = stock;
        this.demandaSemanal = demandaSemanal;
    }

    public double coberturaSemanas() {
        if (demandaSemanal == null || demandaSemanal <= 0) return 0;
        return stock / demandaSemanal;
    }

    public String riesgo() {
        double c = coberturaSemanas();
        if (c >= 4) return "bajo";
        if (c >= 2) return "medio";
        return "alto";
    }

    public Long getId() { return id; }
    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento m) { this.medicamento = m; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Double getDemandaSemanal() { return demandaSemanal; }
    public void setDemandaSemanal(Double d) { this.demandaSemanal = d; }
}
