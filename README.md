# 💊 FarmaPredict Colombia

**Central de abastecimiento farmacéutico con analítica predictiva para IPS y gestores en Colombia.**

FarmaPredict anticipa el desabastecimiento de medicamentos críticos antes de que ocurra:
pronóstico de demanda a 4 semanas, alertas de cobertura por sede y compra sugerida para
mantener la meta operativa **≥ 4 semanas** y reducir quiebres de stock en UCI y tratamientos crónicos.

> Contexto: en Colombia se registran 174 fármacos con fallas de dispensación
> (Auto 1282/2025, Corte Constitucional) y las tutelas por medicamentos crecieron
> +106,8 % entre 2023 y 2024. FarmaPredict convierte ese riesgo en decisiones de compra priorizadas.

---

## ✨ Funcionalidades

| Módulo | Qué hace |
|---|---|
| 🔐 **Auth JWT** | Login con roles `ADMIN`, `QF` (químico farmacéutico) y `USER` (auxiliar). Registro solo para ADMIN. |
| 📊 **Resumen** | KPIs (referencias, críticos, cobertura promedio, compra total), barras de cobertura y panel de compra inmediata. |
| 📦 **Inventario** | Buscador por nombre/código/sede, filtro por riesgo, tabla stock → demanda → cobertura → riesgo con botón Predecir. |
| 🔮 **Pronóstico** | Paso 1: filtra por riesgo (alto/medio/bajo). Paso 2: elige medicamento. Calcula demanda 4 semanas, cobertura, riesgo y compra sugerida (backend o modo demo local). |
| 👥 **Usuarios** | Creación de cuentas (solo ADMIN). |

## 🧱 Stack

- **Backend:** Java 17 · Spring Boot 3.2.5 · Spring Data JPA · Spring Security + JWT · MySQL 8 · Puerto `8081` · DB `farma_db`
- **Frontend:** React 19 · Vite 8 · TailwindCSS 4 · Axios · React Router 7 · Puerto `5173`
- **ML:** Python 3.12 · scikit-learn (script de entrenamiento en `ml/`, la inferencia vive en Java)

## 🚀 Instalación rápida

Requisitos: Java 17, Maven 3.9+, Node 22+, MySQL 8 (usuario `root`/`root`).

```powershell
# 1. Backend (crea farma_db automáticamente)
cd farma-predict/backend
mvn spring-boot:run   # API en http://localhost:8081

# 2. Frontend (en otra terminal)
cd farma-predict/frontend
npm install
npm run dev           # App en http://localhost:5173
```

Usuarios demo (creados por `DataSeeder`):

| Rol | Usuario | Clave |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Químico farmacéutico | `qf` | `qf123` |
| Auxiliar | `aux` | `aux123` |

> Más detalle: [`docs/INSTALACION.md`](docs/INSTALACION.md) · [`docs/GUIA-USO.md`](docs/GUIA-USO.md)

## 🔌 API principal

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/health` | No | Estado del servicio |
| POST | `/api/auth/login` | No | Retorna `{ token, username, rol }` |
| POST | `/api/auth/register` | ADMIN | Crea usuario (`ADMIN`, `QF`, `USER`) |
| GET | `/api/medicamentos` | JWT | Lista con cobertura y riesgo (`?riesgo=alto`) |
| GET | `/api/stats` | JWT | KPIs: total, críticos, cobertura prom, compra total |
| POST | `/api/predict` | JWT | `{ codigo, historial:[...] }` → demanda 4 sem, cobertura, riesgo, compra |

Lógica de riesgo: `cobertura = stock / demandaSemanal` → `bajo ≥ 4` · `medio ≥ 2` · `alto < 2`.
Compra sugerida: `demanda × 4 − stock` (mínimo 0).

> Referencia completa: [`docs/API.md`](docs/API.md) · Modelo: [`docs/MODELO-DATOS.md`](docs/MODELO-DATOS.md)

## 🧠 Pronóstico y datos

- Baseline en `ml/train.py`: RandomForest con rezagos sobre `ml/data_sample.csv` (luego SISMED real + XGBoost).
- El frontend siempre responde: si el backend no está disponible, calcula en **modo demo local** con la misma fórmula y lo indica con un badge.
- Fuentes previstas: SISMED, INVIMA/MVND e histórico de dispensación por sede.

> Detalle: [`docs/ML.md`](docs/ML.md)

## 🗂️ Estructura

```
farmapredict-colombia/
├── backend/   # Spring Boot (model, repository, dto, service, controller, config, security)
├── frontend/  # React + Tailwind (api, context, pages: Login, Dashboard)
├── ml/        # Entrenamiento (train.py, data_sample.csv) — no es runtime
└── docs/      # Documentación del proyecto
```

> Arquitectura y decisiones: [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md)

## 🗺️ Roadmap

- [ ] Carga masiva SISMED + catálogo INVIMA
- [ ] Modelo XGBoost 39 semanas + clasificador de quiebre con SHAP
- [ ] Compras: órdenes, proveedores y punto de reorden automático
- [ ] Alertas WhatsApp/email cuando cobertura < 2 semanas
- [ ] Multi-sede y reportes para Supersalud

## 🤝 Contribución

1. Crea una rama desde `main`: `git checkout -b feat/mi-cambio`
2. Sigue el estilo del repo (Spring + React/Tailwind ya configurados)
3. Abre un PR describiendo el cambio y cómo probarlo

## 📄 Licencia

MIT — ver [`LICENSE`](LICENSE).

## 👤 Autor

Jhonn Luna Acosta — Ingeniero de Sistemas | Tech Lead
LinkedIn: https://www.linkedin.com/in/jhonn-luna-acosta
GitHub: https://github.com/JhonnLunAcosta
