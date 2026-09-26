# Modelo de datos — FarmaPredict Colombia

DB `farma_db` (MySQL 8, `ddl-auto=update`).

## Diagrama
```
usuarios (id, username UNIQUE, password[bcrypt], rol, activo)
medicamentos (id, codigo UNIQUE, nombre, concentracion, categoria)
inventarios (id, medicamento_id FK, sede, stock, demandaSemanal)
  UNIQUE(medicamento_id, sede)
```

## Tablas
### `usuarios`
| Campo | Tipo | Notas |
|---|---|---|
| id | BIGINT AI PK | |
| username | VARCHAR UNIQUE NOT NULL | login |
| password | VARCHAR NOT NULL | BCrypt |
| rol | VARCHAR | `ADMIN`, `QF`, `USER` |
| activo | BOOLEAN | default true |

### `medicamentos`
| Campo | Tipo | Notas |
|---|---|---|
| id | BIGINT AI PK | |
| codigo | VARCHAR(50) UNIQUE NOT NULL | ej. `N05BA01` |
| nombre | VARCHAR(300) NOT NULL | ej. `Norepinefrina 4mg/4mL` |
| concentracion | VARCHAR(100) | ej. `4mg/4mL` |
| categoria | VARCHAR(50) | `UCI`, `CRONICO`, `ANTIBIOTICO`, `GENERAL`, `NEURO` |

### `inventarios`
| Campo | Tipo | Notas |
|---|---|---|
| id | BIGINT AI PK | |
| medicamento_id | FK → medicamentos | |
| sede | VARCHAR(100) NOT NULL | ej. `UCI Central`, `Bogotá Norte` |
| stock | INT NOT NULL | unidades actuales |
| demandaSemanal | DOUBLE NOT NULL | promedio dispensación/semana |

Métodos derivados (`Inventario.java`): `coberturaSemanas() = stock/demandaSemanal`,
`riesgo()` por umbrales 4/2.

## Seed (`DataSeeder`)
3 usuarios + 8 medicamentos (Norepinefrina, Metformina, Acetaminofén, Enalapril,
Amoxicilina, Ceftriaxona, Albúmina, Levetiracetam) con inventario por sede.
Se ejecuta solo si las tablas están vacías.

## Evolución
Previsto: `dispensaciones` (medicamento, sede, fecha, cantidad, precio),
`pronosticos` (histórico de predicciones), `ordenes_compra` y `proveedores`.
