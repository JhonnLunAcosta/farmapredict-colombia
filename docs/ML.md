# ML — Pronóstico de demanda

## Estado actual (MVP)
- **Runtime = Java** (`PronosticoService`): promedio móvil de últimas 8 semanas.
  Sin dependencias pesadas, auditable y suficiente para el MVP.
- **Entrenamiento = Python** (`ml/`): baseline `RandomForestRegressor` con
  rezagos (`lag1`, `lag2`, `ma3`) + `precio_promedio` + `import_share`.

```powershell
cd ml
pip install -r requirements.txt
python train.py   # imprime MAE por medicamento con data_sample.csv
```

## Datos
- Hoy: `ml/data_sample.csv` (sintético, 2 medicamentos × 10 semanas).
- Siguiente: SISMED (precios/transacciones), INVIMA/MVND (disponibilidad) e
  histórico de dispensación por sede (rezagos, medias móviles, tendencia).

## Roadmap modelado
1. XGBoost por medicamento con `TimeSeriesSplit` → pronóstico 39 semanas
   (referencia: R² ≈ 0,99 en prototipos académicos con datos de Bogotá).
2. Clasificador de quiebre (cobertura < 2 sem) con explicabilidad (SHAP/valores
   de importancia) visible en el dashboard.
3. Portar coeficientes a Java o exponer microservicio Python solo para inferencia.

## Validación
Métricas: MAE/RMSE/MAPE para regresión; precisión/recall/F1 para el clasificador
de quiebre. Versionar datasets y modelos antes de cada cambio de umbrales.
