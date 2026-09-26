package com.farmapredict.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensancha columnas del catálogo aunque la tabla ya exista.
 * (Hibernate ddl-auto=update no siempre modifica columnas existentes en MySQL.)
 */
@Component
@Order(1)
public class DbMigrationRunner implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        String[] ddl = {
            "ALTER TABLE medicamentos MODIFY COLUMN nombre VARCHAR(500)",
            "ALTER TABLE medicamentos MODIFY COLUMN concentracion VARCHAR(200)",
            "ALTER TABLE medicamentos MODIFY COLUMN principio_activo VARCHAR(1000)",
            "ALTER TABLE medicamentos MODIFY COLUMN titular VARCHAR(500)",
            "ALTER TABLE medicamentos MODIFY COLUMN registro_sanitario VARCHAR(200)",
            "ALTER TABLE medicamentos MODIFY COLUMN estado_registro VARCHAR(20)",
            "ALTER TABLE medicamentos MODIFY COLUMN categoria VARCHAR(50)",
            "ALTER TABLE medicamentos MODIFY COLUMN codigo VARCHAR(50)"
        };
        for (String sql : ddl) {
            try {
                em.createNativeQuery(sql).executeUpdate();
            } catch (Exception ignored) {
                // Tabla aún no creada (la crea Hibernate después) o motor distinto.
            }
        }
    }
}
