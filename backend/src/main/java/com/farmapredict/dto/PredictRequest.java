package com.farmapredict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class PredictRequest {
    @NotBlank
    private String codigo;
    private String sede = "Bogotá Norte";
    @NotEmpty
    private List<Double> historial;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public List<Double> getHistorial() { return historial; }
    public void setHistorial(List<Double> historial) { this.historial = historial; }
}
