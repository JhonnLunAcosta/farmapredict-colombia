# Guía de uso — FarmaPredict Colombia

## Roles
| Rol | Usuario demo | Puede |
|---|---|---|
| ADMIN | `admin/admin123` | Todo + crear usuarios |
| QF | `qf/qf123` | Inventario y pronósticos |
| USER | `aux/aux123` | Consulta y reportes |

## Flujo recomendado (QF)
1. **Resumen**: revisa KPIs y el panel rojo "Compra inmediata".
2. **Inventario**: busca por nombre/código/sede, filtra por riesgo y pulsa
   **Predecir** en la fila crítica → salta a Pronóstico con datos precargados.
3. **Pronóstico**:
   - Paso 1: elige riesgo (alto/medio/bajo/todos).
   - Paso 2: elige medicamento (verás sede y cobertura).
   - Ajusta el historial si tienes datos reales y pulsa
     **"Predecir demanda y compra sugerida"**.
   - Resultado: cobertura, compra sugerida (meta 4 sem) y gráfica S1–S4.
   - Badge `MODO DEMO` = backend no disponible, cálculo local aplicado.
4. **Actualizar**: botón ↻ recarga medicamentos + stats.

## Catálogo y data real
Pestaña **Catálogo**: busca en el CUM por código, nombre, principio activo o titular
y filtra por estado del registro. Como ADMIN, importa masivos:
- **CUM (INVIMA)**: `datos/cum_ejemplo.csv` → `POST /api/catalogo/importar`.
  Maestro oficial: https://app.invima.gov.co/cum y datasets "Código Único de
  Medicamentos" en https://www.datos.gov.co (quien provee: INVIMA; reportan
  titulares, fabricantes e importadores).
- **Precios SISMED**: `datos/sismed_ejemplo.csv` → `POST /api/catalogo/precios/importar`.
  Dato oficial: Ministerio de Salud/SISPRO (https://web.sispro.gov.co, boletines en
  minsalud.gov.co); reportan fabricantes, importadores y titulares (Circular 21/2026).
- La **dispensación por sede** (lo que alimenta el pronóstico) la provee cada
  IPS/gestor desde su propio software: se carga como historial en Pronóstico o a
  futuro por CSV.

La importación CUM corre **en segundo plano por lotes**: subes el archivo, la app
responde al instante con una barra de progreso y puedes seguir trabajando mientras
se procesan las 65k filas. El catálogo se consulta **paginado de 50** para no
cargar el navegador.

## Admin
Pestaña **Usuarios**: crea cuentas (`USER`, `QF`, `ADMIN`).
Solo visible con rol ADMIN.

## Consejos
- Meta operativa: cobertura **≥ 4 semanas**; bajo 2 = compra inmediata.
- El historial ideal son 8 semanas reales de dispensación por sede.
- Si un pronóstico sale en modo demo, levanta el backend (`:8081`) y repite.
