package com.farmapredict.controller;

import com.farmapredict.model.Medicamento;
import com.farmapredict.model.SismedPrecio;
import com.farmapredict.repository.MedicamentoRepository;
import com.farmapredict.repository.SismedPrecioRepository;
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

    public CatalogoController(MedicamentoRepository meds, SismedPrecioRepository precios) {
        this.meds = meds;
        this.precios = precios;
    }

    @GetMapping
    public List<Medicamento> buscar(@RequestParam(required = false) String q,
                                    @RequestParam(required = false) String estado) {
        List<Medicamento> all = meds.findAll();
        String query = q == null ? "" : q.trim().toLowerCase();
        return all.stream()
                .filter(m -> estado == null || estado.isBlank()
                        || estado.equalsIgnoreCase(m.getEstadoRegistro()))
                .filter(m -> query.isBlank()
                        || m.getCodigo().toLowerCase().contains(query)
                        || m.getNombre().toLowerCase().contains(query)
                        || (m.getPrincipioActivo() != null && m.getPrincipioActivo().toLowerCase().contains(query))
                        || (m.getTitular() != null && m.getTitular().toLowerCase().contains(query)))
                .toList();
    }

    @GetMapping("/{codigo}/precios")
    public List<SismedPrecio> preciosDe(@PathVariable String codigo) {
        return precios.findByCodigoCumOrderByPeriodoDesc(codigo.toUpperCase());
    }

    /**
     * Carga masiva del catálogo CUM (INVIMA).
     * CSV con cabecera:
     * codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario
     */
    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Map<String, Object> importarCum(@RequestParam("file") MultipartFile file) {
        int creados = 0, actualizados = 0;
        List<String> errores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null || !header.toLowerCase().contains("codigo")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "CSV inválido. Cabecera esperada: codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario");
            }
            String line;
            int fila = 1;
            while ((line = br.readLine()) != null) {
                fila++;
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 2 || c[0].isBlank() || c[1].isBlank()) {
                    errores.add("Fila " + fila + ": código y nombre obligatorios");
                    continue;
                }
                String codigo = c[0].trim().toUpperCase();
                Optional<Medicamento> existente = meds.findByCodigo(codigo);
                Medicamento m = existente.orElseGet(Medicamento::new);
                boolean nuevo = existente.isEmpty();
                m.setCodigo(codigo);
                m.setNombre(corta(c[1].trim(), 500));
                if (c.length > 2) m.setConcentracion(corta(c[2].trim(), 200));
                if (c.length > 3 && !c[3].isBlank()) m.setCategoria(c[3].trim().toUpperCase());
                if (c.length > 4) m.setPrincipioActivo(corta(c[4].trim(), 1000));
                if (c.length > 5) m.setTitular(corta(c[5].trim(), 500));
                if (c.length > 6 && !c[6].isBlank()) m.setEstadoRegistro(c[6].trim().toUpperCase());
                if (c.length > 7) m.setRegistroSanitario(corta(c[7].trim(), 200));
                meds.save(m);
                if (nuevo) creados++; else actualizados++;
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo: " + e.getMessage());
        }
        return Map.of("creados", creados, "actualizados", actualizados,
                "errores", errores.size() > 20 ? errores.subList(0, 20) : errores);
    }

    /**
     * Carga masiva de precios SISMED.
     * CSV con cabecera: codigo,periodo,canal,precio_min,precio_max,precio_prom,unidades
     * periodo ej. 2026-T3 · canal INS o COM
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

    private String corta(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
