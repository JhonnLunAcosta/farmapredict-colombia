package com.farmapredict.service;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Importación masiva por lotes JDBC (sin SELECT+INSERT por fila).
 * 65k CUM en segundos-minutos en vez de decenas de minutos con JPA.
 * Corre en segundo plano (@Async) con progreso consultable.
 */
@Service
public class CatalogoImportService {

    public static class Job {
        public final String id = UUID.randomUUID().toString();
        public volatile String estado = "PROCESANDO";
        public volatile int total = 0;
        public final AtomicLong procesados = new AtomicLong();
        public final AtomicLong creados = new AtomicLong();
        public final AtomicLong actualizados = new AtomicLong();
        public final List<String> errores = Collections.synchronizedList(new ArrayList<>());
    }

    private static final int LOTE = 1000;

    private final JdbcTemplate jdbc;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    public CatalogoImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Job crearJob() {
        Job j = new Job();
        jobs.put(j.id, j);
        return j;
    }

    public Optional<Job> ver(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    @Async
    public void procesar(String jobId, byte[] contenido) {
        Job j = jobs.get(jobId);
        if (j == null) return;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(contenido), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null || !header.toLowerCase().contains("codigo")) {
                fallar(j, "CSV inválido. Cabecera esperada: codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario");
                return;
            }
            // 1. Leer y deduplicar por código (última ocurrencia gana)
            Map<String, String[]> porCodigo = new LinkedHashMap<>();
            String line;
            int fila = 1;
            while ((line = br.readLine()) != null) {
                fila++;
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 2 || c[0].isBlank() || c[1].isBlank()) {
                    if (j.errores.size() < 20) j.errores.add("Fila " + fila + ": código y nombre obligatorios");
                    continue;
                }
                porCodigo.put(c[0].trim().toUpperCase(), c);
            }
            j.total = porCodigo.size();

            // 2. Un solo SELECT para saber qué existe
            Set<String> existentes = new HashSet<>(
                    jdbc.queryForList("SELECT codigo FROM medicamentos", String.class));
            List<String[]> ins = new ArrayList<>();
            List<String[]> upd = new ArrayList<>();
            for (var e : porCodigo.entrySet()) {
                (existentes.contains(e.getKey()) ? upd : ins).add(e.getValue());
            }

            // 3. Lotes de 1000
            loteInsert(ins, j);
            loteUpdate(upd, j);
            j.estado = "COMPLETADO";
        } catch (Exception e) {
            fallar(j, e.getMessage());
        }
    }

    private void loteInsert(List<String[]> filas, Job j) {
        String sql = "INSERT INTO medicamentos (codigo,nombre,concentracion,categoria,principio_activo,titular,estado_registro,registro_sanitario) VALUES (?,?,?,?,?,?,?,?)";
        for (List<String[]> chunk : partes(filas)) {
            jdbc.batchUpdate(sql, setter(chunk));
            j.procesados.addAndGet(chunk.size());
            j.creados.addAndGet(chunk.size());
        }
    }

    private void loteUpdate(List<String[]> filas, Job j) {
        String sql = "UPDATE medicamentos SET nombre=?,concentracion=?,categoria=?,principio_activo=?,titular=?,estado_registro=?,registro_sanitario=? WHERE codigo=?";
        for (List<String[]> chunk : partes(filas)) {
            jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String[] c = chunk.get(i);
                    ps.setString(1, corta(col(c, 1), 500));
                    ps.setString(2, corta(col(c, 2), 200));
                    ps.setString(3, vacioToNull(col(c, 3)));
                    ps.setString(4, corta(col(c, 4), 1000));
                    ps.setString(5, corta(col(c, 5), 500));
                    ps.setString(6, vacioToNull(col(c, 6)));
                    ps.setString(7, corta(col(c, 7), 200));
                    ps.setString(8, c[0].trim().toUpperCase());
                }
                @Override public int getBatchSize() { return chunk.size(); }
            });
            j.procesados.addAndGet(chunk.size());
            j.actualizados.addAndGet(chunk.size());
        }
    }

    private BatchPreparedStatementSetter setter(List<String[]> chunk) {
        return new BatchPreparedStatementSetter() {
            @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
                String[] c = chunk.get(i);
                ps.setString(1, c[0].trim().toUpperCase());
                ps.setString(2, corta(col(c, 1), 500));
                ps.setString(3, corta(col(c, 2), 200));
                ps.setString(4, vacioToNull(col(c, 3)));
                ps.setString(5, corta(col(c, 4), 1000));
                ps.setString(6, corta(col(c, 5), 500));
                ps.setString(7, vacioToNull(col(c, 6)));
                ps.setString(8, corta(col(c, 7), 200));
            }
            @Override public int getBatchSize() { return chunk.size(); }
        };
    }

    private List<List<String[]>> partes(List<String[]> filas) {
        List<List<String[]>> out = new ArrayList<>();
        for (int i = 0; i < filas.size(); i += LOTE) {
            out.add(filas.subList(i, Math.min(i + LOTE, filas.size())));
        }
        return out;
    }

    private String col(String[] c, int i) {
        return c.length > i ? c[i].trim() : "";
    }

    private String vacioToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.toUpperCase();
    }

    private String corta(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private void fallar(Job j, String msg) {
        j.estado = "FALLIDO";
        j.errores.add(msg == null ? "Error desconocido" : msg);
    }
}
