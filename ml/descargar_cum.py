"""Descarga CUM (datos.gov.co) y convierte a CSV del importador.
Destino: datos/cum_vigentes.csv
Columnas: codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario

Fuentes (mismo esquema, distinto estado):
  i7cb-raxc -> VIGENTE      (listado oficial vigentes)
  vgr4-gemg -> RENOVACION   (en trámite de renovación)
  vwwf-4ftk -> VENCIDO      (vencidos)
  spzp-dfuc -> OTRO         (otros estados)
Prioridad ante duplicados: VIGENTE > RENOVACION > VENCIDO > OTRO.
"""
import csv
import json
import urllib.request

PAGE = 50000
PRIORIDAD = {"OTRO": 0, "VENCIDO": 1, "RENOVACION": 2, "VIGENTE": 3}
FUENTES = [
    ("spzp-dfuc", "OTRO"),
    ("vwwf-4ftk", "VENCIDO"),
    ("vgr4-gemg", "RENOVACION"),
    ("i7cb-raxc", "VIGENTE"),
]

ATC_CAT = {
    "A": "GENERAL", "B": "UCI", "C": "CRONICO", "D": "GENERAL",
    "G": "GENERAL", "H": "GENERAL", "J": "ANTIBIOTICO", "L": "ONCO",
    "M": "GENERAL", "N": "NEURO", "P": "GENERAL", "R": "GENERAL",
    "S": "GENERAL", "V": "GENERAL",
}


def fetch(dataset, offset):
    url = (f"https://www.datos.gov.co/resource/{dataset}.json"
           f"?$limit={PAGE}&$offset={offset}&$order=expedientecum,consecutivocum")
    req = urllib.request.Request(url, headers={"User-Agent": "FarmaPredict/1.0"})
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.load(r)


def clean(s):
    return (s or "").replace("\n", " ").replace("\r", " ").strip()


def nombre_comercial(r):
    producto = clean(r.get("producto"))
    cantidad = clean(r.get("cantidad"))
    um = clean(r.get("unidadmedida"))
    forma = clean(r.get("formafarmaceutica"))
    partes = [producto]
    dosis = f"{cantidad} {um}".strip()
    if dosis:
        partes.append(dosis)
    if forma:
        partes.append(f"({forma})")
    return " ".join(partes)


def concentracion_txt(r):
    cantidad = clean(r.get("cantidad"))
    um = clean(r.get("unidadmedida"))
    dosis = f"{cantidad} {um}".strip()
    return dosis or clean(r.get("concentracion"))


def fila_csv(r, estado):
    codigo = f"{clean(r.get('expedientecum'))}-{clean(r.get('consecutivocum'))}"
    atc = clean(r.get("atc"))
    return {
        "codigo": codigo,
        "nombre": nombre_comercial(r) or clean(r.get("descripcioncomercial")),
        "concentracion": concentracion_txt(r),
        "categoria": ATC_CAT.get(atc[:1].upper(), "GENERAL"),
        "principio_activo": clean(r.get("principioactivo")),
        "titular": clean(r.get("titular")),
        "estado": estado,
        "registro_sanitario": clean(r.get("registrosanitario")),
    }


def main():
    por_codigo = {}
    for dataset, estado in FUENTES:
        n = 0
        offset = 0
        while True:
            rows = fetch(dataset, offset)
            if not rows:
                break
            for r in rows:
                f = fila_csv(r, estado)
                if not f["codigo"].strip("-") or not f["nombre"]:
                    continue
                actual = por_codigo.get(f["codigo"])
                if actual is None or PRIORIDAD[estado] >= PRIORIDAD[actual["estado"]]:
                    por_codigo[f["codigo"]] = f
                    n += 1
            offset += PAGE
        print(f"{dataset} ({estado}): vistos, unicos acumulados={len(por_codigo)}", flush=True)

    with open("datos/cum_vigentes.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["codigo", "nombre", "concentracion", "categoria",
                                          "principio_activo", "titular", "estado",
                                          "registro_sanitario"])
        w.writeheader()
        for codigo in sorted(por_codigo):
            w.writerow(por_codigo[codigo])

    conteo = {}
    for v in por_codigo.values():
        conteo[v["estado"]] = conteo.get(v["estado"], 0) + 1
    print(f"OK datos/cum_vigentes.csv filas={len(por_codigo)} por estado={conteo}")


if __name__ == "__main__":
    main()
