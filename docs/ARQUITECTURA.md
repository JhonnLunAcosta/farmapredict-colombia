# Arquitectura — FarmaPredict Colombia

## Estilo
Monorepo con 3 piezas desacopladas que se comunican por HTTP/JSON:

```
[React 19 + Tailwind] --axios/JWT--> [Spring Boot :8081 + MySQL farma_db]
        │
        └── [ml/train.py] (offline: entrena baseline, no es runtime)
```

## Decisiones
1. **Mismo stack de la organización** (Spring Boot + MySQL + React) para reutilizar
   conocimiento de `citas-medicas`/`citas-frontend` (JPA, JWT, Tailwind).
2. **Backend en 8081** para no chocar con citas (8080) en desarrollo local.
3. **Seguridad JWT** idéntica a citas: `JwtService` + `JwtAuthFilter` +
   `CustomUserDetailsService`; `/api/auth/login` y `/api/health` públicos,
   `/api/auth/register` solo `ADMIN`, resto autenticado.
4. **Inferencia en Java** (`PronosticoService`): promedio últimas 8 semanas,
   `cobertura = stock/demanda`, umbrales 4/2, `compra = demanda×4 − stock`.
   El ML en Python solo entrena; los coeficientes se portan a Java.
5. **Frontend resiliente**: si `POST /api/predict` falla, calcula en modo demo
   local con la misma fórmula y lo señaliza (badge + toast). El botón nunca queda muerto.
6. **Identidad visual propia** (verde farmacia, nada de Vitalis): login institucional
   con foto + moléculas animadas, dashboard con topbar + hero + KPIs.

## Flujo de predicción
```
Historial semanal (≥3 valores) → promedio 8 sem → demanda
  → cobertura = stock/demanda → riesgo (4/2) → compra = demanda×4 − stock
  → respuesta { demandaNext4s[4], coberturaSemanas, riesgo, compraSugerida }
```

## Evolución prevista
SISMED/INVIMA → XGBoost 39 sem + clasificador de quiebre → órdenes de compra →
alertas < 2 sem → reportes Supersalud. Ver `docs/ML.md` y `README.md` (Roadmap).
