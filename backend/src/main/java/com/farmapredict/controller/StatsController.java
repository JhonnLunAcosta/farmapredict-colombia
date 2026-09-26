package com.farmapredict.controller;

import com.farmapredict.repository.InventarioRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final InventarioRepository invRepo;

    public StatsController(InventarioRepository invRepo) {
        this.invRepo = invRepo;
    }

    @GetMapping
    public Map<String, Object> stats() {
        var all = invRepo.findAll();
        long criticos = all.stream().filter(i -> i.riesgo().equals("alto")).count();
        long medio = all.stream().filter(i -> i.riesgo().equals("medio")).count();
        double coberturaProm = all.stream().mapToDouble(i -> i.coberturaSemanas()).average().orElse(0);
        coberturaProm = Math.round(coberturaProm * 10.0) / 10.0;
        long compraTotal = all.stream()
                .mapToLong(i -> Math.max(0, (long) Math.round(i.getDemandaSemanal() * 4 - i.getStock())))
                .sum();
        return Map.of(
                "total", all.size(),
                "criticos", criticos,
                "medio", medio,
                "coberturaProm", coberturaProm,
                "compraTotal", compraTotal);
    }
}
