"""Baseline: predice demanda próxima semana con RandomForest + rezagos."""
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error

df = pd.read_csv("data_sample.csv")

def make_features(g):
    g = g.sort_values("semana").copy()
    g["lag1"] = g["dispensacion"].shift(1)
    g["lag2"] = g["dispensacion"].shift(2)
    g["ma3"] = g["dispensacion"].shift(1).rolling(3).mean()
    return g.dropna()

out = []
for cod, g in df.groupby("codigo"):
    f = make_features(g)
    X = f[["lag1", "lag2", "ma3", "precio_promedio", "import_share"]]
    y = f["dispensacion"]
    train_X, test_X = X.iloc[:-2], X.iloc[-2:]
    train_y, test_y = y.iloc[:-2], y.iloc[-2:]
    m = RandomForestRegressor(n_estimators=100, random_state=42)
    m.fit(train_X, train_y)
    pred = m.predict(test_X)
    mae = mean_absolute_error(test_y, pred)
    print(f"{cod} MAE={mae:.1f} real={list(test_y)} pred={[round(p,1) for p in pred]}")
    out.append((cod, mae))

print("OK baseline. Siguiente: SISMED real + XGBoost + clasificador desabastecimiento.")
