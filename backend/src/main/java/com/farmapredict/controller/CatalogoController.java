package com.farmapredict.controller;

import com.farmapredict.model.Medicamento;
import com.farmapredict.model.SismedPrecio;
import com.farmapredict.repository.MedicamentoRepository;
import com.farmapredict.repository.SismedPrecioRepository;
import com.farmapredict.service.CatalogoImportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final MedicamentoRepository meds;
    private final SismedPrecioRepository precios;
    private final CatalogoImportService importService;

    public CatalogoController(MedicamentoRepository meds, SismedPrecioRepository precios,
                              CatalogoImportService importService) {
        this.meds = meds;
        this.precios = precios;
        this.importService = importService;
    }

    /** Catálogo paginado: evita traer 65k filas al front. */
    @GetMapping
    public Map<String, Object> buscar(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String estado,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        String query = q == null ? "" : q.trim();
        String est = (estado == null || estado.isBlank()) ? null : estado.trim().toUpperCase();
        Page<Medicamento> p = meds.buscar(query, est,
                PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size))));
        return Map.of(
                "content", p.getContent(),
                "totalElements", p.getTotalElements(),
                "totalPages", p.getTotalPages(),
                "page", p.getNumber());
    }

    @GetMapping("/{codigo}/precios")
    public List<SismedPrecio> preciosDe(@PathVariable String codigo) {
        return precios.findByCodigoCumOrderByPeriodoDesc(codigo.toUpperCase());
    }

    /**
     * Carga masiva CUM por lotes en segundo plano.
     * Responde al instante con jobId; el progreso se consulta en GET /api/catalogo/import/{jobId}.
     * CSV: codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario
     */
    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> importarCum(@RequestParam("file") MultipartFile file) {
        try {
            CatalogoImportService.Job job = importService.crearJob();
            importService.procesar(job.id, file.getBytes());
            return Map.of("jobId", job.id, "estado", job.estado);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/import/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> estadoImport(@PathVariable String jobId) {
        return importService.ver(jobId)
                .map(j -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("jobId", j.id);
                    r.put("estado", j.estado);
                    r.put("total", j.total);
                    r.put("procesados", j.procesados.get());
                    r.put("creados", j.creados.get());
                    r.put("actualizados", j.actualizados.get());
                    r.put("errores", j.errores.size() > 20 ? j.errores.subList(0, 20) : List.copyOf(j.errores));
                    return r;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job no encontrado"));
    }

    /**
     * Carga masiva de precios SISMED (archivos pequeños: sincrónica).
     * CSV: codigo,periodo,canal,precio_min,precio_max,precio_prom,unidades
     */
    @PostMapping("/precios/importar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> importarPrecios(@RequestParam("file") MultipartFile file) {
        int creados = 0, actualizados = 0;
        List<String> errores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null || !header.toLowerCase().contains("codigo")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "CSV inválido. Cabecera esperada: codigo,periodo,canal,precio_min,precio_max,precio_prom,unidades");
            }
            String line;
            int fila = 1;
            while ((line = br.readLine()) != null) {
                fila++;
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 7 || c[0].isBlank() || c[1].isBlank() || c[2].isBlank()) {
                    errores.add("Fila " + fila + ": codigo, periodo y canal obligatorios");
                    continue;
                }
                try {
                    String codigo = c[0].trim().toUpperCase();
                    String periodo = c[1].trim();
                    String canal = c[2].trim().toUpperCase();
                    Optional<SismedPrecio> ex = precios.findByCodigoCumAndPeriodoAndCanal(codigo, periodo, canal);
                    SismedPrecio p = ex.orElseGet(SismedPrecio::new);
                    boolean nuevo = ex.isEmpty();
                    p.setCodigoCum(codigo);
                    p.setPeriodo(periodo);
                    p.setCanal(canal);
                    p.setPrecioMin(num(c[3]));
                    p.setPrecioMax(num(c[4]));
                    p.setPrecioProm(num(c[5]));
                    p.setUnidades(c[6].isBlank() ? null : Long.parseLong(c[6].trim()));
                    precios.save(p);
                    if (nuevo) creados++; else actualizados++;
                } catch (Exception e) {
                    errores.add("Fila " + fila + ": " + e.getMessage());
                }
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo: " + e.getMessage());
        }
        return Map.of("creados", creados, "actualizados", actualizados,
                "errores", errores.size() > 20 ? errores.subList(0, 20) : errores);
    }

    private Double num(String s) {
        if (s == null || s.isBlank()) return null;
        return Double.parseDouble(s.trim());
    }
}
