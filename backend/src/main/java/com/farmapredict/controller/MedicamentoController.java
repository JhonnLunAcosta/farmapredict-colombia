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
    public List<MedicamentoRiesgoDTO> list(@RequestParam(required = false) String riesgo,
                                           @RequestParam(required = false) String q) {
        String query = q == null ? "" : q.trim().toLowerCase();
        return invRepo.findAll().stream()
                .filter(i -> {
                    if (query.isBlank()) return true;
                    var m = i.getMedicamento();
                    return m.getCodigo().toLowerCase().contains(query)
                            || m.getNombre().toLowerCase().contains(query)
                            || (m.getPrincipioActivo() != null && m.getPrincipioActivo().toLowerCase().contains(query))
                            || i.getSede().toLowerCase().contains(query);
                })
                .map(i -> new MedicamentoRiesgoDTO(
                        i.getMedicamento().getCodigo(),
                        i.getMedicamento().getNombre(),
                        i.getMedicamento().getPrincipioActivo(),
                        i.getMedicamento().getTitular(),
                        i.getSede(), i.getStock(), i.getDemandaSemanal(),
                        Math.round(i.coberturaSemanas() * 10.0) / 10.0,
                        i.riesgo()))
                .filter(d -> riesgo == null || riesgo.isBlank() || d.getRiesgo().equals(riesgo))
                .toList();
    }
}
