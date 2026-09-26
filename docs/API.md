# API — FarmaPredict Colombia

Base: `http://localhost:8081` · Auth: `Authorization: Bearer <token>` salvo indicados.

## Salud
`GET /api/health` (público) → `{ "status": "ok", "service": "farmapredict" }`

## Auth
- `POST /api/auth/login` (público)
  Request: `{ "username": "qf", "password": "qf123" }`
  Response: `{ "token": "eyJ...", "username": "qf", "rol": "QF" }`
- `POST /api/auth/register` (solo `ADMIN`)
  Request: `{ "username": "nuevo", "password": "clave123", "rol": "USER" }`
  Roles válidos: `ADMIN`, `QF`, `USER`. Response igual que login.

## Medicamentos
`GET /api/medicamentos?riesgo=alto` (JWT)
```json
[
  { "codigo": "N05BA01", "nombre": "Norepinefrina 4mg/4mL", "sede": "UCI Central",
    "stock": 180, "demandaSemanal": 120.0, "coberturaSemanas": 1.5, "riesgo": "alto" }
]
```

## Stats
`GET /api/stats` (JWT)
```json
{ "total": 8, "criticos": 3, "medio": 1, "coberturaProm": 2.9, "compraTotal": 2450 }
```

## Pronóstico
`POST /api/predict` (JWT)
Request:
```json
{ "codigo": "N05BA01", "historial": [110, 125, 118, 140, 135, 150, 148, 160] }
```
Response:
```json
{ "codigo": "N05BA01", "demandaNext4s": [135.8, 135.8, 135.8, 135.8],
  "coberturaSemanas": 1.3, "riesgo": "alto", "compraSugerida": 363 }
```
Reglas: promedio últimas 8 semanas; `cobertura = stock/demanda`;
riesgo `bajo ≥ 4`, `medio ≥ 2`, `alto < 2`; `compra = demanda×4 − stock` (≥ 0).
Errores: `401` sin token, `400` historial inválido, `404` código inexistente (según evolución).

## Catálogo CUM (INVIMA) y precios SISMED
- `GET /api/catalogo?q=&estado=&page=0&size=50` (JWT) — **paginado** (máx 200 por página).
  Responde `{ content, totalElements, totalPages, page }`. `estado`: `VIGENTE`, `RENOVACION`, `VENCIDO`.
- `GET /api/catalogo/{codigo}/precios` (JWT) — precios SISMED por periodo/canal.
- `POST /api/catalogo/importar` (solo `ADMIN`, multipart `file` CSV) — carga masiva CUM
  **por lotes JDBC en segundo plano** (1 SELECT + lotes de 1000, no 130k queries).
  Responde al instante `{ jobId, estado }`; el progreso se consulta en:
- `GET /api/catalogo/import/{jobId}` (solo `ADMIN`) —
  `{ estado: PROCESANDO|COMPLETADO|FALLIDO, total, procesados, creados, actualizados, errores }`.
  Cabecera CSV: `codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario`
- `POST /api/catalogo/precios/importar` (solo `ADMIN`, multipart `file` CSV, sincrónico).
  Cabecera: `codigo,periodo,canal,precio_min,precio_max,precio_prom,unidades` (periodo ej. `2026-T3`, canal `INS`/`COM`)

## Vista de lectura
`v_inventario_riesgo` (creada al arrancar): inventario + cobertura/riesgo/compra
calculados en SQL, para reportes sin cargar JPA.

## Códigos de error comunes
| Código | Cuándo |
|---|---|
| 401 | Sin token o expirado (re-login) |
| 403 | Rol insuficiente (ej. register sin ADMIN) |
| 400 | Validación (historial < 3 valores, campos vacíos) |
