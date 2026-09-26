import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ROLES = [
  { rol: "Administrador", username: "admin", password: "admin123", desc: "Gestión total y usuarios", accent: "#10b981" },
  { rol: "Químico Farmacéutico", username: "qf", password: "qf123", desc: "Inventario y pronósticos", accent: "#06b6d4" },
  { rol: "Auxiliar", username: "aux", password: "aux123", desc: "Consulta y reportes", accent: "#a3e635" },
];

const TICKER = ["Norepinefrina", "Metformina", "Ceftriaxona", "Enalapril", "Amoxicilina", "Levetiracetam", "Albúmina", "Acetaminofén"];

function CapsuleLogo({ size = 46 }) {
  return (
    <div className="flex items-center justify-center rounded-2xl"
      style={{ width: size, height: size, background: "linear-gradient(135deg,#10b981,#065f46)", boxShadow: "0 14px 30px -10px rgba(16,185,129,.65)" }}>
      <svg width={size * 0.58} height={size * 0.58} viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2.2" strokeLinecap="round">
        <rect x="3" y="8" width="18" height="8" rx="4" />
        <path d="M12 8v8" />
        <path d="M7 12h.01M9.5 12h.01" strokeWidth="3" />
      </svg>
    </div>
  );
}

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [ver, setVer] = useState(false);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const [imgOk, setImgOk] = useState(true);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      await login(form.username, form.password);
      nav("/");
    } catch {
      setError("No pudimos verificar tus credenciales. Revisa usuario y contraseña.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex flex-col xl:flex-row" style={{ background: "#06231f" }}>
      {/* Panel institucional farma */}
      <div className="relative overflow-hidden xl:w-[52%] min-h-[46vh] xl:min-h-screen flex flex-col justify-end">
        {imgOk && (
          <img
            src="https://images.unsplash.com/photo-1587854692152-cbe660dbde88?q=80&w=1600&auto=format&fit=crop"
            alt="Farmacia institucional"
            onError={() => setImgOk(false)}
            className="absolute inset-0 w-full h-full object-cover"
          />
        )}
        <div className="absolute inset-0" style={{ background: "linear-gradient(160deg,rgba(4,32,28,.94) 0%,rgba(4,47,40,.82) 42%,rgba(6,95,70,.55) 75%,rgba(16,185,129,.35) 100%)" }} />
        <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full pharma-blob" />
        <div className="absolute bottom-10 right-10 w-72 h-72 rounded-full pharma-blob2" />

        {/* moléculas flotantes */}
        <svg className="absolute top-10 right-12 w-40 h-40 pharma-float opacity-70" viewBox="0 0 100 100" fill="none" stroke="#6ee7b7" strokeWidth="1.6">
          <circle cx="30" cy="30" r="10" fill="rgba(110,231,183,.15)" />
          <circle cx="70" cy="38" r="6" fill="rgba(110,231,183,.25)" />
          <circle cx="52" cy="68" r="12" fill="rgba(6,182,212,.18)" />
          <path d="M38 36l24-2M62 42l-6 18M38 38l8 22" />
        </svg>
        <svg className="absolute top-24 left-10 w-24 h-24 pharma-float2 opacity-80" viewBox="0 0 24 24" fill="none" stroke="#a7f3d0" strokeWidth="1.8" strokeLinecap="round">
          <rect x="2" y="8.5" width="20" height="7" rx="3.5" fill="rgba(167,243,208,.15)" />
          <path d="M12 8.5v7" />
        </svg>

        <div className="relative p-8 sm:p-12 text-white">
          <div className="flex items-center gap-3">
            <CapsuleLogo />
            <div>
              <p className="text-2xl font-black tracking-tight">FarmaPredict</p>
              <p className="text-[11px] uppercase tracking-[0.28em] text-emerald-300">Inteligencia farmacéutica · Colombia</p>
            </div>
          </div>

          <h1 className="mt-8 text-4xl sm:text-5xl font-black leading-[1.05] tracking-tight">
            Anticipa el<br />desabastecimiento<br />
            <span className="text-emerald-300">antes de que ocurra.</span>
          </h1>
          <p className="mt-4 max-w-lg text-sm sm:text-base text-emerald-50/85">
            Pronóstico de demanda a 4 semanas, alertas de cobertura y compra sugerida
            para los medicamentos críticos del país. Meta operativa ≥ 4 semanas.
          </p>

          <div className="mt-6 flex flex-wrap gap-2">
            {["Cobertura ≥ 4 sem", "Alertas 24/7", "SISMED + INVIMA", "UCI + crónicos"].map((c) => (
              <span key={c} className="text-xs font-semibold px-3 py-1.5 rounded-full border border-emerald-300/30 bg-emerald-400/10 backdrop-blur-sm">{c}</span>
            ))}
          </div>

          <div className="mt-8 overflow-hidden rounded-xl border border-white/10 bg-black/25 backdrop-blur-sm">
            <div className="pharma-marquee flex gap-8 whitespace-nowrap px-4 py-2.5 text-xs font-semibold text-emerald-100/90">
              {[...TICKER, ...TICKER].map((t, i) => (
                <span key={i} className="flex items-center gap-2"><span className="w-1.5 h-1.5 rounded-full bg-emerald-300 inline-block" />{t}</span>
              ))}
            </div>
          </div>
          <p className="mt-4 text-[11px] text-emerald-100/50">Auto 1282/2025 · Corte Constitucional · Vigilancia INVIMA</p>
        </div>
      </div>

      {/* Panel acceso */}
      <div className="flex-1 flex items-center justify-center px-5 py-10 sm:px-12" style={{ background: "linear-gradient(180deg,#f2fbf7,#e7f6ee)" }}>
        <div className="w-full max-w-md fade-up">
          <div className="rounded-3xl bg-white/85 backdrop-blur-xl border border-emerald-100 p-7 sm:p-8" style={{ boxShadow: "0 24px 60px -24px rgba(6,95,70,.35)" }}>
            <div className="flex items-center gap-3">
              <CapsuleLogo size={40} />
              <div>
                <h2 className="text-2xl font-black tracking-tight text-emerald-950">Bienvenido</h2>
                <p className="text-sm text-emerald-900/60">Accede a tu central de abastecimiento</p>
              </div>
            </div>

            <form onSubmit={submit} className="mt-6 space-y-4">
              <div>
                <label className="lbl">Usuario</label>
                <input className="field" placeholder="ej: qf" autoComplete="username"
                  value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
              </div>
              <div>
                <label className="lbl">Contraseña</label>
                <div className="relative">
                  <input type={ver ? "text" : "password"} className="field pr-20" placeholder="••••••••"
                    autoComplete="current-password"
                    value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
                  <button type="button" onClick={() => setVer(!ver)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-bold text-emerald-700 hover:text-emerald-900">
                    {ver ? "Ocultar" : "Ver"}
                  </button>
                </div>
              </div>

              {error && (
                <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 px-3 py-2.5">
                  <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-600 text-xs font-bold text-white">!</span>
                  <p className="text-sm text-red-700">{error}</p>
                </div>
              )}

              <button disabled={cargando}
                className="w-full rounded-xl py-3.5 text-white font-bold text-sm tracking-wide transition hover:brightness-110 active:scale-[.99] disabled:opacity-60"
                style={{ background: "linear-gradient(135deg,#059669,#065f46)", boxShadow: "0 14px 30px -12px rgba(5,150,105,.7)" }}>
                {cargando ? "Verificando credenciales…" : "Entrar a mi central →"}
              </button>
            </form>

            <div className="mt-6">
              <p className="text-[11px] font-bold uppercase tracking-[0.18em] text-emerald-900/40">Cuentas demo · un clic</p>
              <div className="mt-2 grid gap-2">
                {ROLES.map((a) => (
                  <button key={a.username} type="button"
                    onClick={() => { setForm({ username: a.username, password: a.password }); setError(""); }}
                    className={`flex items-center gap-3 rounded-2xl border px-3 py-2.5 text-left transition hover:shadow-md ${form.username === a.username ? "border-emerald-500 bg-emerald-50" : "border-slate-200 bg-white hover:border-emerald-300"}`}>
                    <span className="w-1.5 self-stretch rounded-full" style={{ background: a.accent }} />
                    <span className="flex-1">
                      <span className="block text-sm font-bold text-emerald-950">{a.rol} <span className="font-mono text-xs text-slate-400">· {a.username}</span></span>
                      <span className="block text-xs text-slate-500">{a.desc}</span>
                    </span>
                    <span className="text-xs font-bold text-emerald-700">Usar →</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
          <p className="mt-4 text-center text-xs text-emerald-900/50">Protegido con JWT · Tus datos están seguros · Cumplimiento INVIMA</p>
        </div>
      </div>
    </div>
  );
}
