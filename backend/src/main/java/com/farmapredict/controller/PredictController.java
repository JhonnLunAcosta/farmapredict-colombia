package com.farmapredict.controller;

import com.farmapredict.dto.PredictRequest;
import com.farmapredict.dto.PredictResponse;
import com.farmapredict.service.PronosticoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predict")
public class PredictController {

    private final PronosticoService service;

    public PredictController(PronosticoService service) {
        this.service = service;
    }

    @PostMapping
    public PredictResponse predict(@Valid @RequestBody PredictRequest req) {
        return service.predecir(req.getCodigo(), req.getHistorial());
    }
}
