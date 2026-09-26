import { useEffect, useMemo, useState } from "react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext";

const riskStyle = (r) =>
  r === "alto"
    ? { bg: "#fee2e2", fg: "#b91c1c", dot: "#dc2626" }
    : r === "medio"
      ? { bg: "#fef3c7", fg: "#b45309", dot: "#d97706" }
      : { bg: "#d1fae5", fg: "#047857", dot: "#10b981" };

function RiskPill({ riesgo }) {
  const s = riskStyle(riesgo);
  return (
    <span className="inline-flex items-center gap-1.5 text-[11px] font-bold px-2.5 py-1 rounded-full" style={{ background: s.bg, color: s.fg }}>
      <span className={`w-1.5 h-1.5 rounded-full ${riesgo === "alto" ? "pharma-ping" : ""}`} style={{ background: s.dot }} />
      {riesgo.toUpperCase()}
    </span>
  );
}

function computeLocal(codigo, historial, meds) {
  const h = historial.filter((x) => Number.isFinite(x) && x > 0);
  if (h.length < 3) throw new Error("Historial insuficiente");
  const ult = h.slice(-8);
  const demanda = ult.reduce((a, b) => a + b, 0) / ult.length;
  const med = meds.find((m) => m.codigo === codigo);
  const stock = med?.stock ?? 1000;
  const cobertura = Math.round((stock / demanda) * 10) / 10;
  const riesgo = cobertura >= 4 ? "bajo" : cobertura >= 2 ? "medio" : "alto";
  const compra = Math.max(0, Math.round(demanda * 4 - stock));
  const d = Math.round(demanda * 10) / 10;
  return { codigo, demandaNext4s: [d, d, d, d], coberturaSemanas: cobertura, riesgo, compraSugerida: compra, origen: "local" };
}

export default function Dashboard() {
  const { user, logout } = useAuth();
  const esAdmin = user?.rol === "ADMIN";
  const [tab, setTab] = useState("resumen");
  const [meds, setMeds] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [q, setQ] = useState("");
  const [fRiesgo, setFRiesgo] = useState("");
  const [prRiesgo, setPrRiesgo] = useState("alto");
  const [form, setForm] = useState({ codigo: "N05BA01", historial: "110, 125, 118, 140, 135, 150, 148, 160" });
  const [catalogo, setCatalogo] = useState([]);
  const [cq, setCq] = useState("");
  const [cest, setCest] = useState("");
  const [importing, setImporting] = useState(false);
  const [pred, setPred] = useState(null);
  const [predicting, setPredicting] = useState(false);

  const showToast = (texto, tipo = "ok") => {
    setToast({ texto, tipo });
    setTimeout(() => setToast(null), 4200);
  };

  const load = async () => {
    setLoading(true);
    try {
      const [m, s] = await Promise.all([api.get("/api/medicamentos"), api.get("/api/stats")]);
      setMeds(m.data);
      setStats(s.data);
    } catch {
      showToast("Sin conexión al backend (:8081). Revisa que Spring Boot esté corriendo.", "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const criticos = useMemo(() => meds.filter((m) => m.riesgo === "alto"), [meds]);
  const filtrados = useMemo(() => meds.filter((m) =>
    `${m.nombre} ${m.codigo} ${m.sede} ${m.principioActivo || ""} ${m.titular || ""}`.toLowerCase().includes(q.toLowerCase()) &&
    (!fRiesgo || m.riesgo === fRiesgo)
  ), [meds, q, fRiesgo]);

  const listaPron = useMemo(() => {
    const l = prRiesgo ? meds.filter((m) => m.riesgo === prRiesgo) : meds;
    return l.length ? l : meds;
  }, [meds, prRiesgo]);

  const elegirRiesgoPron = (r) => {
    setPrRiesgo(r);
    const l = r ? meds.filter((m) => m.riesgo === r) : meds;
    const primero = (l.length ? l : meds)[0];
    if (primero) {
      const base = primero.demandaSemanal || 100;
      const hist = [0.92, 0.97, 1.0, 1.06, 0.98, 1.03, 1.0, 1.02].map((f) => Math.round(base * f));
      setForm({ codigo: primero.codigo, historial: hist.join(", ") });
      setPred(null);
    }
  };

  const parseHistorial = () => {
    const vals = form.historial.split(/[,\s;]+/).map((x) => Number(x.trim())).filter((x) => x !== "" && Number.isFinite(x));
    if (vals.length < 3 || vals.some((v) => v <= 0)) throw new Error("Escribe al menos 3 valores semanales positivos separados por coma.");
    return vals;
  };

  const predecir = async (e) => {
    e?.preventDefault();
    setPredicting(true);
    setPred(null);
    try {
      const historial = parseHistorial();
      try {
        const { data } = await api.post("/api/predict", { codigo: form.codigo, historial });
        setPred({ ...data, origen: "backend" });
        showToast(`Pronóstico listo para ${form.codigo} · cobertura ${data.coberturaSemanas} sem.`);
      } catch {
        const local = computeLocal(form.codigo, historial, meds);
        setPred(local);
        showToast("Backend no disponible: cálculo local aplicado (modo demo).", "warn");
      }
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setPredicting(false);
    }
  };

  const predecirFila = async (m) => {
    const base = m.demandaSemanal || 100;
    const hist = [0.92, 0.97, 1.0, 1.06, 0.98, 1.03, 1.0, 1.02].map((f) => Math.round(base * f));
    setPrRiesgo(m.riesgo || "");
    setForm({ codigo: m.codigo, historial: hist.join(", ") });
    setTab("pronostico");
    setPredicting(true);
    setPred(null);
    try {
      try {
        const { data } = await api.post("/api/predict", { codigo: m.codigo, historial: hist });
        setPred({ ...data, origen: "backend" });
      } catch {
        setPred(computeLocal(m.codigo, hist, meds));
        showToast("Modo demo local: backend no disponible.", "warn");
      }
    } finally {
      setPredicting(false);
    }
  };

  const loadCatalogo = async () => {
    try {
      const params = new URLSearchParams();
      if (cq) params.set("q", cq);
      if (cest) params.set("estado", cest);
      const { data } = await api.get(`/api/catalogo?${params.toString()}`);
      setCatalogo(data);
    } catch {
      showToast("No se pudo cargar el catálogo. ¿Backend en :8081?", "error");
    }
  };

  useEffect(() => { if (tab === "catalogo") loadCatalogo(); }, [tab]);

  const subirArchivo = async (e, tipo) => {
    const f = e.target.files?.[0];
    if (!f) return;
    setImporting(true);
    try {
      const fd = new FormData();
      fd.append("file", f);
      const url = tipo === "cum" ? "/api/catalogo/importar" : "/api/catalogo/precios/importar";
      const { data } = await api.post(url, fd, { headers: { "Content-Type": "multipart/form-data" } });
      showToast(`Importación ${tipo.toUpperCase()}: ${data.creados} creados · ${data.actualizados} actualizados${data.errores?.length ? ` · ${data.errores.length} errores` : ""}.`);
      loadCatalogo();
      load();
    } catch (err) {
      showToast(err.response?.data?.error || err.response?.data?.message || "Importación fallida (solo ADMIN).", "error");
    } finally {
      setImporting(false);
      e.target.value = "";
    }
  };

  const kpis = [
    { t: "Referencias activas", v: stats?.total ?? meds.length, s: "sedes Bogotá + UCI", g: "linear-gradient(135deg,#10b981,#047857)" },
    { t: "En riesgo alto", v: stats?.criticos ?? criticos.length, s: "requieren compra ya", g: "linear-gradient(135deg,#f43f5e,#b91c1c)" },
    { t: "Cobertura promedio", v: `${stats?.coberturaProm ?? "—"} sem`, s: "meta ≥ 4 semanas", g: "linear-gradient(135deg,#06b6d4,#0369a1)" },
    { t: "Compra sugerida", v: stats?.compraTotal ?? "—", s: "unidades próximas 4 sem", g: "linear-gradient(135deg,#84cc16,#4d7c0f)" },
  ];

  return (
    <div className="min-h-screen" style={{ background: "#f1f8f4" }}>
      {/* Topbar farma */}
      <header className="sticky top-0 z-20 backdrop-blur-xl bg-white/85 border-b border-emerald-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-3 flex items-center gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl flex items-center justify-center" style={{ background: "linear-gradient(135deg,#10b981,#065f46)" }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round"><rect x="3" y="8" width="18" height="8" rx="4" /><path d="M12 8v8" /></svg>
            </div>
            <div>
              <p className="font-black tracking-tight text-emerald-950 leading-none">FarmaPredict</p>
              <p className="text-[10px] uppercase tracking-[0.24em] text-emerald-600">Central de abastecimiento</p>
            </div>
          </div>
          <nav className="ml-6 hidden md:flex items-center gap-1 bg-emerald-50 border border-emerald-100 rounded-full p-1">
            {[["resumen", "Resumen"], ["inventario", "Inventario"], ["pronostico", "Pronóstico"], ["catalogo", "Catálogo"]].map(([id, label]) => (
              <button key={id} onClick={() => setTab(id)}
                className={`text-xs font-bold px-4 py-2 rounded-full transition ${tab === id ? "text-white shadow" : "text-emerald-900/60 hover:text-emerald-900"}`}
                style={tab === id ? { background: "linear-gradient(135deg,#059669,#065f46)" } : undefined}>
                {label}
              </button>
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-2">
            <span className="hidden sm:inline-flex text-[11px] font-bold px-3 py-1.5 rounded-full bg-emerald-100 text-emerald-800">{user?.username} · {user?.rol}</span>
            <button onClick={load} className="text-xs font-bold px-3 py-2 rounded-xl border border-emerald-200 text-emerald-800 hover:bg-emerald-50">↻ Actualizar</button>
            <button onClick={logout} className="text-xs font-bold px-3 py-2 rounded-xl text-white" style={{ background: "#0f172a" }}>Salir</button>
          </div>
        </div>
        <div className="md:hidden px-4 pb-3 flex gap-2">
          {[["resumen", "Resumen"], ["inventario", "Inventario"], ["pronostico", "Pronóstico"], ["catalogo", "Catálogo"]].map(([id, label]) => (
            <button key={id} onClick={() => setTab(id)} className={`flex-1 text-xs font-bold px-3 py-2 rounded-xl border ${tab === id ? "text-white border-transparent" : "bg-white text-emerald-900 border-emerald-200"}`}
              style={tab === id ? { background: "linear-gradient(135deg,#059669,#065f46)" } : undefined}>{label}</button>
          ))}
        </div>
      </header>

      {/* Hero */}
      <div className="relative overflow-hidden text-white" style={{ background: "linear-gradient(120deg,#04231e 0%,#065f46 55%,#059669 100%)" }}>
        <div className="absolute -right-20 -top-24 w-96 h-96 rounded-full pharma-blob" />
        <svg className="absolute right-8 bottom-4 w-44 h-24 opacity-40" viewBox="0 0 200 60" fill="none" stroke="#6ee7b7" strokeWidth="2.5" strokeLinecap="round">
          <path d="M0 30h30l8-16 12 32 9-22 6 6h25l8-14 12 28 9-18 5 4H200" />
        </svg>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 py-8 flex flex-wrap items-end gap-4">
          <div>
            <p className="text-[11px] uppercase tracking-[0.24em] text-emerald-300">Operación de hoy · {new Date().toLocaleDateString("es-CO", { weekday: "long", day: "numeric", month: "long" })}</p>
            <h1 className="mt-1 text-3xl sm:text-4xl font-black tracking-tight">¿Qué se puede agotar esta semana?</h1>
            <p className="mt-1 text-sm text-emerald-100/85">Prioriza compras por riesgo, no por intuición. Cobertura objetivo ≥ 4 semanas.</p>
          </div>
          <div className="ml-auto flex items-center gap-2 rounded-2xl bg-black/25 border border-white/15 px-4 py-3 backdrop-blur-sm">
            <span className={`w-2.5 h-2.5 rounded-full ${criticos.length ? "pharma-ping bg-red-400" : "bg-emerald-300"}`} />
            <div>
              <p className="text-2xl font-black leading-none">{criticos.length}</p>
              <p className="text-[11px] text-emerald-100/80">medicamentos en riesgo alto</p>
            </div>
          </div>
        </div>
      </div>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-5">
        {/* KPIs */}
        <div className="grid sm:grid-cols-2 xl:grid-cols-4 gap-3">
          {kpis.map((k) => (
            <div key={k.t} className="rounded-3xl bg-white border border-emerald-100 p-5 flex items-center gap-4 shadow-sm hover:shadow-lg transition">
              <div className="w-12 h-12 rounded-2xl text-white flex items-center justify-center text-xl font-black" style={{ background: k.g }}>◈</div>
              <div>
                <p className="text-[11px] uppercase tracking-wider text-slate-500 font-bold">{k.t}</p>
                <p className="text-2xl font-black text-slate-900">{loading ? "…" : k.v}</p>
                <p className="text-xs text-slate-400">{k.s}</p>
              </div>
            </div>
          ))}
        </div>

        {tab === "resumen" && (
          <div className="grid xl:grid-cols-5 gap-4">
            <div className="xl:col-span-3 rounded-3xl bg-white border border-emerald-100 p-6">
              <h2 className="font-black text-emerald-950">Nivel de cobertura por referencia</h2>
              <p className="text-xs text-slate-500">Barra = semanas de stock frente a la meta de 6 sem.</p>
              <div className="mt-4 space-y-3">
                {loading && <p className="text-sm text-slate-400">Cargando inventario…</p>}
                {meds.slice(0, 8).map((m) => (
                  <div key={m.codigo} className="flex items-center gap-3">
                    <span className="w-40 truncate text-xs font-bold text-slate-700">{m.nombre}</span>
                    <div className="flex-1 h-2.5 rounded-full bg-slate-100 overflow-hidden">
                      <div className="h-full rounded-full transition-all" style={{
                        width: `${Math.min(100, (m.coberturaSemanas / 6) * 100)}%`,
                        background: m.riesgo === "alto" ? "linear-gradient(90deg,#f43f5e,#b91c1c)" : m.riesgo === "medio" ? "linear-gradient(90deg,#fbbf24,#d97706)" : "linear-gradient(90deg,#34d399,#059669)",
                      }} />
                    </div>
                    <span className="text-xs w-16 text-right font-mono">{m.coberturaSemanas} sem</span>
                    <RiskPill riesgo={m.riesgo} />
                  </div>
                ))}
              </div>
            </div>
            <div className="xl:col-span-2 rounded-3xl p-6 text-white" style={{ background: "linear-gradient(160deg,#7f1d1d,#b91c1c)" }}>
              <h2 className="font-black">Compra inmediata</h2>
              <p className="text-xs text-red-100/85">{criticos.length} referencias bajo 2 semanas de cobertura.</p>
              <div className="mt-3 space-y-2 max-h-72 overflow-auto">
                {criticos.map((m) => (
                  <div key={m.codigo} className="rounded-2xl bg-white/12 border border-white/20 p-3 flex items-center gap-2 backdrop-blur-sm">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-bold truncate">{m.nombre}</p>
                      <p className="text-[11px] text-red-100/80">{m.sede} · {m.coberturaSemanas} sem · stock {m.stock}</p>
                    </div>
                    <button onClick={() => predecirFila(m)} className="text-[11px] font-bold px-3 py-2 rounded-xl bg-white text-red-700 hover:scale-105 transition">Predecir →</button>
                  </div>
                ))}
                {!criticos.length && !loading && <p className="text-sm">Sin críticos. Buen nivel de cobertura.</p>}
              </div>
            </div>
          </div>
        )}

        {tab === "inventario" && (
          <div className="space-y-3">
            <div className="rounded-3xl bg-white border border-emerald-100 p-4 grid sm:grid-cols-3 gap-2">
              <input className="field" placeholder="🔍 Buscar nombre, código o sede…" value={q} onChange={(e) => setQ(e.target.value)} />
              <select className="field" value={fRiesgo} onChange={(e) => setFRiesgo(e.target.value)}>
                <option value="">Riesgo: todos</option>
                <option value="alto">🔴 Alto</option><option value="medio">🟡 Medio</option><option value="bajo">🟢 Bajo</option>
              </select>
              <button onClick={load} className="rounded-xl border border-emerald-200 text-emerald-800 text-sm font-bold hover:bg-emerald-50">↻ Recargar datos</button>
            </div>
            <div className="rounded-3xl bg-white border border-emerald-100 overflow-hidden">
              <table className="tbl">
                <thead><tr><th>Medicamento</th><th>Sede</th><th>Stock</th><th>Dem/sem</th><th>Cobertura</th><th>Riesgo</th><th></th></tr></thead>
                <tbody>
                  {filtrados.map((m) => (
                    <tr key={m.codigo + m.sede}>
                      <td><b>{m.nombre}</b><br /><span className="text-xs text-slate-400 font-mono">{m.codigo}</span></td>
                      <td>{m.sede}</td><td className="font-mono">{m.stock}</td><td className="font-mono">{m.demandaSemanal}</td>
                      <td className="font-mono font-bold">{m.coberturaSemanas} sem</td>
                      <td><RiskPill riesgo={m.riesgo} /></td>
                      <td><button onClick={() => predecirFila(m)} className="text-[11px] font-bold px-3 py-1.5 rounded-xl text-white" style={{ background: "linear-gradient(135deg,#059669,#065f46)" }}>Predecir</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {!filtrados.length && !loading && <p className="p-6 text-sm text-slate-500">Sin resultados para ese filtro.</p>}
            </div>
          </div>
        )}

        {tab === "pronostico" && (
          <div className="grid lg:grid-cols-2 gap-4">
            <form onSubmit={predecir} className="rounded-3xl bg-white border border-emerald-100 p-6 space-y-4">
              <div>
                <h2 className="font-black text-emerald-950 text-lg">Generar pronóstico de demanda</h2>
                <p className="text-xs text-slate-500">Promedio de últimas 8 semanas · meta de cobertura 4 semanas.</p>
              </div>
              <div>
                <label className="lbl">1 · Filtra por nivel de riesgo</label>
                <select className="field" value={prRiesgo} onChange={(e) => elegirRiesgoPron(e.target.value)}>
                  <option value="alto">🔴 Riesgo alto — compra inmediata ({meds.filter((m) => m.riesgo === "alto").length})</option>
                  <option value="medio">🟡 Riesgo medio — vigilar ({meds.filter((m) => m.riesgo === "medio").length})</option>
                  <option value="bajo">🟢 Riesgo bajo — estable ({meds.filter((m) => m.riesgo === "bajo").length})</option>
                  <option value="">Todos los niveles ({meds.length})</option>
                </select>
              </div>
              <div>
                <label className="lbl">2 · Medicamento en riesgo {prRiesgo || "seleccionado"} ({listaPron.length})</label>
                <select className="field" value={form.codigo} onChange={(e) => {
                  const cod = e.target.value;
                  const sel = listaPron.find((m) => m.codigo === cod) || meds.find((m) => m.codigo === cod);
                  const base = sel?.demandaSemanal || 100;
                  const hist = [0.92, 0.97, 1.0, 1.06, 0.98, 1.03, 1.0, 1.02].map((f) => Math.round(base * f));
                  setForm({ codigo: cod, historial: hist.join(", ") });
                  setPred(null);
                }}>
                  {listaPron.map((m) => (
                    <option key={m.codigo} value={m.codigo}>
                      {m.codigo} — {m.nombre} · {m.sede} · {m.coberturaSemanas} sem
                    </option>
                  ))}
                </select>
                {(() => {
                  const sel = meds.find((m) => m.codigo === form.codigo);
                  return sel ? (
                    <div className="mt-2 flex items-center gap-2 text-xs">
                      <RiskPill riesgo={sel.riesgo} />
                      <span className="text-slate-500">{sel.sede} · stock {sel.stock} · dem/sem {sel.demandaSemanal} · cobertura {sel.coberturaSemanas} sem</span>
                    </div>
                  ) : null;
                })()}
              </div>
              <div>
                <label className="lbl">Dispensación semanal histórica (mín. 3 valores)</label>
                <textarea className="field font-mono" rows={3} value={form.historial} onChange={(e) => setForm({ ...form, historial: e.target.value })} />
                <p className="text-[11px] text-slate-400 mt-1">Ej: 110, 125, 118, 140, 135, 150, 148, 160</p>
              </div>
              <button disabled={predicting}
                className="w-full rounded-2xl py-3.5 text-white font-black text-sm tracking-wide transition hover:brightness-110 active:scale-[.99] disabled:opacity-60"
                style={{ background: "linear-gradient(135deg,#059669,#064e3b)", boxShadow: "0 14px 30px -12px rgba(5,150,105,.7)" }}>
                {predicting ? "⏳ Calculando pronóstico…" : "⚡ Predecir demanda y compra sugerida"}
              </button>
            </form>
            <div className="rounded-3xl p-6 text-white" style={{ background: "linear-gradient(160deg,#04231e,#065f46)" }}>
              <h2 className="font-black">Resultado del pronóstico</h2>
              {!pred && !predicting && <p className="text-sm text-emerald-100/70 mt-2">Completa el formulario y pulsa predecir. Si el backend no responde, se calcula en modo demo local.</p>}
              {predicting && <p className="mt-3 text-sm text-emerald-100 animate-pulse">Analizando historial y stock…</p>}
              {pred && (
                <div className="mt-4 space-y-3">
                  <div className="flex items-center gap-2">
                    <p className="font-mono font-bold">{pred.codigo}</p>
                    <RiskPill riesgo={pred.riesgo} />
                    {pred.origen === "local" && <span className="text-[10px] font-bold px-2 py-1 rounded-full bg-amber-300 text-amber-950">MODO DEMO</span>}
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    <div className="rounded-2xl bg-white/10 border border-white/15 p-3">
                      <p className="text-[11px] text-emerald-100/70">Cobertura actual</p>
                      <p className="text-2xl font-black">{pred.coberturaSemanas} <span className="text-xs font-normal">sem</span></p>
                    </div>
                    <div className="rounded-2xl bg-emerald-300 text-emerald-950 p-3">
                      <p className="text-[11px] font-bold">Compra sugerida</p>
                      <p className="text-2xl font-black">{pred.compraSugerida} <span className="text-xs font-normal">uds</span></p>
                    </div>
                  </div>
                  <div className="flex items-end gap-2 h-28 rounded-2xl bg-black/25 border border-white/10 p-3">
                    {pred.demandaNext4s.map((d, i) => (
                      <div key={i} className="flex-1 flex flex-col items-center gap-1">
                        <span className="text-[10px] font-mono text-emerald-100">{d}</span>
                        <div className="w-full rounded-t-lg pharma-bar" style={{ height: `${Math.max(12, (d / Math.max(...pred.demandaNext4s)) * 72)}px` }} />
                        <span className="text-[10px] text-emerald-100/60">S{i + 1}</span>
                      </div>
                    ))}
                  </div>
                  <p className="text-[11px] text-emerald-100/60">Demanda estimada próximas 4 semanas · {pred.origen === "backend" ? "calculado por el servidor" : "cálculo local de demostración"}.</p>
                </div>
              )}
            </div>
          </div>
        )}

        {tab === "catalogo" && (
          <div className="space-y-3">
            <div className="rounded-3xl bg-white border border-emerald-100 p-4 grid sm:grid-cols-4 gap-2">
              <input className="field sm:col-span-2" placeholder="🔍 Buscar código, nombre, principio activo o titular…"
                value={cq} onChange={(e) => setCq(e.target.value)} onKeyDown={(e) => e.key === "Enter" && loadCatalogo()} />
              <select className="field" value={cest} onChange={(e) => setCest(e.target.value)}>
                <option value="">Estado: todos</option>
                <option value="VIGENTE">Vigente</option>
                <option value="RENOVACION">En renovación</option>
                <option value="VENCIDO">Vencido</option>
              </select>
              <button onClick={loadCatalogo} className="rounded-xl text-white text-sm font-bold"
                style={{ background: "linear-gradient(135deg,#059669,#065f46)" }}>Buscar en CUM</button>
            </div>
            <p className="text-[11px] text-emerald-900/50">Fuente maestra: CUM INVIMA (app.invima.gov.co/cum) + datos.gov.co · Precios: SISMED/SISPRO · {catalogo.length} resultados</p>

            {esAdmin && (
              <div className="rounded-3xl bg-white border border-emerald-100 p-4 grid sm:grid-cols-2 gap-3">
                <label className="rounded-2xl border-2 border-dashed border-emerald-200 p-4 text-center cursor-pointer hover:bg-emerald-50 transition">
                  <p className="text-sm font-bold text-emerald-900">📤 Importar CUM (CSV)</p>
                  <p className="text-[11px] text-slate-500">codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro · ver <span className="font-mono">datos/cum_ejemplo.csv</span></p>
                  <input type="file" accept=".csv" className="hidden" disabled={importing} onChange={(e) => subirArchivo(e, "cum")} />
                  <span className="mt-2 inline-block text-xs font-bold px-3 py-1.5 rounded-xl text-white" style={{ background: "#065f46" }}>
                    {importing ? "Importando…" : "Elegir archivo"}
                  </span>
                </label>
                <label className="rounded-2xl border-2 border-dashed border-cyan-200 p-4 text-center cursor-pointer hover:bg-cyan-50 transition">
                  <p className="text-sm font-bold text-cyan-900">📤 Importar precios SISMED (CSV)</p>
                  <p className="text-[11px] text-slate-500">codigo,periodo,canal,precio_min,precio_max,precio_prom,unidades · ver <span className="font-mono">datos/sismed_ejemplo.csv</span></p>
                  <input type="file" accept=".csv" className="hidden" disabled={importing} onChange={(e) => subirArchivo(e, "precios")} />
                  <span className="mt-2 inline-block text-xs font-bold px-3 py-1.5 rounded-xl text-white" style={{ background: "#0369a1" }}>
                    {importing ? "Importando…" : "Elegir archivo"}
                  </span>
                </label>
              </div>
            )}

            <div className="rounded-3xl bg-white border border-emerald-100 overflow-hidden">
              <table className="tbl">
                <thead><tr><th>CUM</th><th>Medicamento</th><th>Principio activo</th><th>Titular</th><th>Estado</th></tr></thead>
                <tbody>
                  {catalogo.map((m) => (
                    <tr key={m.id}>
                      <td className="font-mono text-xs">{m.codigo}</td>
                      <td><b>{m.nombre}</b><br /><span className="text-xs text-slate-400">{m.concentracion} · {m.categoria} · {m.registroSanitario}</span></td>
                      <td className="text-xs">{m.principioActivo || "—"}</td>
                      <td className="text-xs">{m.titular || "—"}</td>
                      <td><span className={`badge ${m.estadoRegistro === "VIGENTE" ? "badge-green" : m.estadoRegistro === "RENOVACION" ? "badge-amber" : "badge-red"}`}>{m.estadoRegistro}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {!catalogo.length && <p className="p-6 text-sm text-slate-500">Pulsa "Buscar en CUM" para cargar el catálogo.</p>}
            </div>
          </div>
        )}
      </main>

      <footer className="max-w-7xl mx-auto px-4 sm:px-6 pb-8 text-center text-[11px] text-emerald-900/50">
        FarmaPredict · Inteligencia farmacéutica para Colombia · SISMED · INVIMA · Cobertura objetivo ≥ 4 semanas
      </footer>

      {toast && (
        <div className={`fixed bottom-5 right-5 z-50 max-w-sm rounded-2xl px-4 py-3 text-sm font-semibold shadow-2xl border backdrop-blur-xl ${toast.tipo === "error" ? "bg-red-600 text-white border-red-400" : toast.tipo === "warn" ? "bg-amber-400 text-amber-950 border-amber-200" : "bg-emerald-600 text-white border-emerald-400"}`}>
          {toast.texto}
        </div>
      )}
    </div>
  );
}
