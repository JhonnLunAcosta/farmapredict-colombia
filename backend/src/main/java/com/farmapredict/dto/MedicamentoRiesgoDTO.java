package com.farmapredict.dto;

public class MedicamentoRiesgoDTO {
    private String codigo;
    private String nombre;
    private String principioActivo;
    private String titular;
    private String sede;
    private int stock;
    private double demandaSemanal;
    private double coberturaSemanas;
    private String riesgo;

    public MedicamentoRiesgoDTO(String codigo, String nombre, String principioActivo, String titular,
                                String sede, int stock, double demandaSemanal, double coberturaSemanas, String riesgo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.principioActivo = principioActivo;
        this.titular = titular;
        this.sede = sede;
        this.stock = stock;
        this.demandaSemanal = demandaSemanal;
        this.coberturaSemanas = coberturaSemanas;
        this.riesgo = riesgo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getPrincipioActivo() { return principioActivo; }
    public String getTitular() { return titular; }
    public String getSede() { return sede; }
    public int getStock() { return stock; }
    public double getDemandaSemanal() { return demandaSemanal; }
    public double getCoberturaSemanas() { return coberturaSemanas; }
    public String getRiesgo() { return riesgo; }
}
