"""Descarga CUM vigentes (datos.gov.co i7cb-raxc) y convierte a CSV del importador.
Destino: datos/cum_vigentes.csv
Columnas: codigo,nombre,concentracion,categoria,principio_activo,titular,estado,registro_sanitario
"""
import csv
import json
import urllib.request

BASE = "https://www.datos.gov.co/resource/i7cb-raxc.json"
PAGE = 50000

ATC_CAT = {
    "A": "GENERAL", "B": "UCI", "C": "CRONICO", "D": "GENERAL",
    "G": "GENERAL", "H": "GENERAL", "J": "ANTIBIOTICO", "L": "ONCO",
    "M": "GENERAL", "N": "NEURO", "P": "GENERAL", "R": "GENERAL",
    "S": "GENERAL", "V": "GENERAL",
}


def fetch(offset):
    url = f"{BASE}?$limit={PAGE}&$offset={offset}&$order=expedientecum,consecutivocum"
    req = urllib.request.Request(url, headers={"User-Agent": "FarmaPredict/1.0"})
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.load(r)


def clean(s):
    return (s or "").replace("\n", " ").replace("\r", " ").replace("�", "").strip()


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


def main():
    vistos = set()
    total = 0
    with open("datos/cum_vigentes.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["codigo", "nombre", "concentracion", "categoria",
                    "principio_activo", "titular", "estado", "registro_sanitario"])
        offset = 0
        while True:
            rows = fetch(offset)
            if not rows:
                break
            for r in rows:
                codigo = f"{clean(r.get('expedientecum'))}-{clean(r.get('consecutivocum'))}"
                if not codigo.strip("-") or codigo in vistos:
                    continue
                vistos.add(codigo)
                nombre = nombre_comercial(r) or clean(r.get("descripcioncomercial"))
                atc = clean(r.get("atc"))
                categoria = ATC_CAT.get(atc[:1].upper(), "GENERAL")
                w.writerow([
                    codigo,
                    nombre,
                    concentracion_txt(r),
                    categoria,
                    clean(r.get("principioactivo")),
                    clean(r.get("titular")),
                    "VIGENTE",
                    clean(r.get("registrosanitario")),
                ])
                total += 1
            print(f"offset={offset} acumulados={total}", flush=True)
            offset += PAGE
    print(f"OK datos/cum_vigentes.csv filas={total}")


if __name__ == "__main__":
    main()
