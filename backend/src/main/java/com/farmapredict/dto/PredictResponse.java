package com.farmapredict.dto;

import java.util.List;

public class PredictResponse {
    private String codigo;
    private List<Double> demandaNext4s;
    private double coberturaSemanas;
    private String riesgo;
    private int compraSugerida;

    public PredictResponse(String codigo, List<Double> demandaNext4s, double coberturaSemanas, String riesgo, int compraSugerida) {
        this.codigo = codigo;
        this.demandaNext4s = demandaNext4s;
        this.coberturaSemanas = coberturaSemanas;
        this.riesgo = riesgo;
        this.compraSugerida = compraSugerida;
    }

    public String getCodigo() { return codigo; }
    public List<Double> getDemandaNext4s() { return demandaNext4s; }
    public double getCoberturaSemanas() { return coberturaSemanas; }
    public String getRiesgo() { return riesgo; }
    public int getCompraSugerida() { return compraSugerida; }
}
