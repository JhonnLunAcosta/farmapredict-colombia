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

## Códigos de error comunes
| Código | Cuándo |
|---|---|
| 401 | Sin token o expirado (re-login) |
| 403 | Rol insuficiente (ej. register sin ADMIN) |
| 400 | Validación (historial < 3 valores, campos vacíos) |
