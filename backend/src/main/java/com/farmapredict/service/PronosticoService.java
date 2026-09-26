package com.farmapredict.service;

import com.farmapredict.dto.PredictResponse;
import com.farmapredict.model.Inventario;
import com.farmapredict.model.Medicamento;
import com.farmapredict.repository.InventarioRepository;
import com.farmapredict.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class PronosticoService {

    private final MedicamentoRepository medRepo;
    private final InventarioRepository invRepo;

    public PronosticoService(MedicamentoRepository medRepo, InventarioRepository invRepo) {
        this.medRepo = medRepo;
        this.invRepo = invRepo;
    }

    public PredictResponse predecir(String codigo, List<Double> historial) {
        List<Double> h = historial.size() > 8 ? historial.subList(historial.size() - 8, historial.size()) : historial;
        double demanda = h.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        int stock = invRepo.findAll().stream()
                .filter(i -> i.getMedicamento().getCodigo().equals(codigo))
                .mapToInt(Inventario::getStock).findFirst().orElse(1000);

        double cobertura = demanda > 0 ? Math.round((stock / demanda) * 10.0) / 10.0 : 0;
        String riesgo = cobertura >= 4 ? "bajo" : cobertura >= 2 ? "medio" : "alto";
        int compra = Math.max(0, (int) Math.round(demanda * 4 - stock));

        List<Double> next4 = Collections.nCopies(4, Math.round(demanda * 10.0) / 10.0);
        return new PredictResponse(codigo, next4, cobertura, riesgo, compra);
    }
}
