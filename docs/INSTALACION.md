# Instalación — FarmaPredict Colombia

## Requisitos
| Herramienta | Versión |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9+ |
| Node.js / npm | 22+ / 10+ |
| MySQL Server | 8.x |
| Python (solo ML) | 3.12 + scikit-learn, pandas |

## 1. Base de datos
MySQL corriendo en `localhost:3306` con usuario `root` / clave `root`
(ver `backend/src/main/resources/application.properties`).
La DB `farma_db` se crea sola (`createDatabaseIfNotExist=true`) y el esquema
con `ddl-auto=update`. Para cambiar credenciales edita ese archivo.

## 2. Backend
```powershell
cd farma-predict/backend
mvn spring-boot:run
```
- API: http://localhost:8081 · Swagger no incluido; usa `/api/health` para verificar.
- Seed automático (`DataSeeder`): usuarios `admin/admin123`, `qf/qf123`,
  `aux/aux123` + 8 medicamentos con inventario por sede.
- Compilar sin correr: `mvn -q compile -DskipTests`

## 3. Frontend
```powershell
cd farma-predict/frontend
npm install
npm run dev
```
- App: http://localhost:5173 (proxy `/api` → `http://localhost:8081`).
- Build prod: `npm run build` (salida en `dist/`).

## 4. ML (opcional)
```powershell
cd farma-predict/ml
pip install -r requirements.txt
python train.py
```
Entrena el baseline RandomForest con `data_sample.csv` e imprime MAE por medicamento.

## Problemas comunes
| Síntoma | Causa / solución |
|---|---|
| `No plugin found for prefix 'spring-boot'` | Corriste Maven desde `farma-predict/`; debe ser desde `farma-predict/backend`. |
| Login 401 | Backend abajo o credenciales mal; verifica `:8081/api/health` y el seed. |
| Frontend sin estilos | `vite.config.js` requiere plugin `@tailwindcss/vite`; corre `npm install` y reinicia `npm run dev`. |
| Toast "modo demo" en pronóstico | Backend no disponible; el cálculo es local. Levanta el backend para modo servidor. |
