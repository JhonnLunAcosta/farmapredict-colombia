package com.farmapredict.controller;

import com.farmapredict.dto.MedicamentoRiesgoDTO;
import com.farmapredict.repository.InventarioRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final InventarioRepository invRepo;

    public MedicamentoController(InventarioRepository invRepo) {
        this.invRepo = invRepo;
    }

    @GetMapping
    public List<MedicamentoRiesgoDTO> list(@RequestParam(required = false) String riesgo) {
        return invRepo.findAll().stream()
                .map(i -> new MedicamentoRiesgoDTO(
                        i.getMedicamento().getCodigo(),
                        i.getMedicamento().getNombre(),
                        i.getSede(), i.getStock(), i.getDemandaSemanal(),
                        Math.round(i.coberturaSemanas() * 10.0) / 10.0,
                        i.riesgo()))
                .filter(d -> riesgo == null || d.getRiesgo().equals(riesgo))
                .toList();
    }
}
