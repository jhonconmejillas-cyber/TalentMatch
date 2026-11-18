#!/usr/bin/env python3
"""Informe de resultados de pruebas del taller (punto 5).

Cruza los reportes XML de Surefire (backend/target/surefire-reports) con el plan de pruebas
(docs/pruebas/plan-de-pruebas.csv) y genera:

  - reporte-pruebas.html : tabla del plan con el resultado real de cada caso + resto de la suite
  - reporte-pruebas.json : los mismos datos en JSON
  - resumen.md           : resumen en Markdown (el pipeline lo agrega al resumen del job)

Uso:
  python3 scripts/reporte_pruebas.py --surefire backend/target/surefire-reports \
      --plan docs/pruebas/plan-de-pruebas.csv --salida reporte-pruebas
"""
import argparse
import csv
import glob
import html
import json
import os
import re
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

CASO = re.compile(r"^(CP\d+)\s*\|")


def leer_surefire(carpeta):
    """Devuelve la lista de pruebas ejecutadas con su estado y mensaje."""
    pruebas = []
    for archivo in sorted(glob.glob(os.path.join(carpeta, "TEST-*.xml"))):
        raiz = ET.parse(archivo).getroot()
        for tc in raiz.iter("testcase"):
            estado, mensaje = "PASSED", ""
            for etiqueta, valor in (("failure", "FAILED"), ("error", "ERROR"), ("skipped", "SKIPPED")):
                nodo = tc.find(etiqueta)
                if nodo is not None:
                    estado = valor
                    mensaje = (nodo.get("message") or nodo.get("type") or "").strip()
                    break
            nombre = tc.get("name", "")
            m = CASO.match(nombre)
            pruebas.append({
                "clase": tc.get("classname", ""),
                "nombre": nombre,
                "caso": m.group(1) if m else None,
                "estado": estado,
                "tiempo": float(tc.get("time") or 0),
                "mensaje": mensaje,
            })
    return pruebas


def leer_plan(ruta):
    with open(ruta, encoding="utf-8") as f:
        return list(csv.DictReader(f, delimiter="|"))


def construir(plan, pruebas):
    por_caso = {p["caso"]: p for p in pruebas if p["caso"]}
    filas = []
    for c in plan:
        p = por_caso.get(c["caso"])
        filas.append({**c,
                      "estado": p["estado"] if p else "NO EJECUTADO",
                      "real": ("Coincide con lo esperado" if p and p["estado"] == "PASSED"
                               else (p["mensaje"] if p else "El caso no se ejecutó")),
                      "tiempo": p["tiempo"] if p else 0})
    total = {k: sum(1 for p in pruebas if p["estado"] == k) for k in ("PASSED", "FAILED", "ERROR", "SKIPPED")}
    total["EJECUTADAS"] = len(pruebas)
    plan_ok = sum(1 for f in filas if f["estado"] == "PASSED")
    clases = {}
    for p in pruebas:
        c = clases.setdefault(p["clase"], {"clase": p["clase"], "total": 0, "PASSED": 0, "FAILED": 0,
                                           "ERROR": 0, "SKIPPED": 0, "tiempo": 0.0})
        c["total"] += 1
        c[p["estado"]] += 1
        c["tiempo"] += p["tiempo"]
    fallas = [p for p in pruebas if p["estado"] in ("FAILED", "ERROR")]
    return {
        "generado": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC"),
        "commit": os.environ.get("GITHUB_SHA", "")[:7],
        "rama": os.environ.get("GITHUB_REF_NAME", ""),
        "totales": total,
        "plan": {"casos": len(filas), "exitosos": plan_ok, "filas": filas},
        "clases": sorted(clases.values(), key=lambda c: c["clase"]),
        "fallas": fallas,
    }


ICONO = {"PASSED": "✅ Passed", "FAILED": "❌ Failed", "ERROR": "💥 Error", "SKIPPED": "⏭️ Skipped",
         "NO EJECUTADO": "⚠️ No ejecutado"}


def a_html(d):
    e = html.escape
    t = d["totales"]
    tarjetas = "".join(
        f'<div class="card {c}"><b>{v}</b><span>{n}</span></div>' for n, v, c in (
            ("Casos ejecutados", t["EJECUTADAS"], ""), ("Exitosos (passed)", t["PASSED"], "ok"),
            ("Fallidos (failed)", t["FAILED"], "ko" if t["FAILED"] else ""),
            ("Errores", t["ERROR"], "ko" if t["ERROR"] else ""), ("Omitidos", t["SKIPPED"], "")))
    plan = "".join(
        f"<tr class='{f['estado'].lower().replace(' ', '-')}'><td><b>{e(f['caso'])}</b></td><td>{e(f['componente'])}</td>"
        f"<td><code>{e(f['metodo'])}</code></td><td>{e(f['descripcion'])}</td><td>{e(f['entrada'])}</td>"
        f"<td>{e(f['tipo'])}</td><td>{e(f['esperado'])}</td><td>{e(f['real'])}</td>"
        f"<td>{ICONO.get(f['estado'], e(f['estado']))}</td><td>{f['tiempo']:.3f} s</td></tr>"
        for f in d["plan"]["filas"])
    clases = "".join(
        f"<tr><td><code>{e(c['clase'].split('.')[-1])}</code></td><td>{c['total']}</td><td>{c['PASSED']}</td>"
        f"<td>{c['FAILED']}</td><td>{c['ERROR']}</td><td>{c['SKIPPED']}</td><td>{c['tiempo']:.2f} s</td></tr>"
        for c in d["clases"])
    fallas = "".join(
        f"<li><b>{e(p['clase'].split('.')[-1])} › {e(p['nombre'])}</b> ({p['estado']})<pre>{e(p['mensaje'])}</pre></li>"
        for p in d["fallas"]) or "<li>Ninguna: todas las pruebas pasaron.</li>"
    return f"""<!doctype html><html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>Informe de pruebas · TalentMatch</title>
<style>
:root{{--bg:#fff;--fg:#1f2328;--muted:#59636e;--line:#d1d9e0;--ok:#1a7f37;--ko:#cf222e;--head:#f6f8fa}}
@media (prefers-color-scheme:dark){{:root{{--bg:#0d1117;--fg:#e6edf3;--muted:#9198a1;--line:#3d444d;--ok:#3fb950;--ko:#f85149;--head:#151b23}}}}
body{{font:14px/1.5 system-ui,sans-serif;background:var(--bg);color:var(--fg);margin:0;padding:24px 16px}}
main{{max-width:1300px;margin:auto}} h1{{margin:0 0 4px}} .meta{{color:var(--muted)}}
.cards{{display:flex;gap:12px;flex-wrap:wrap;margin:20px 0}} .card{{border:1px solid var(--line);border-radius:8px;padding:12px 16px;min-width:140px}}
.card b{{display:block;font-size:28px}} .card span{{color:var(--muted)}} .ok b{{color:var(--ok)}} .ko b{{color:var(--ko)}}
.tabla{{overflow-x:auto}} table{{border-collapse:collapse;width:100%;margin:8px 0 24px}}
th,td{{border:1px solid var(--line);padding:6px 8px;text-align:left;vertical-align:top}} th{{background:var(--head)}}
tr.failed td,tr.error td,tr.no-ejecutado td{{color:var(--ko)}} pre{{white-space:pre-wrap;color:var(--ko)}}
</style></head><body><main>
<h1>Informe de resultados de pruebas · TalentMatch</h1>
<div class="meta">Generado {e(d['generado'])} · rama {e(d['rama'] or 'local')} · commit {e(d['commit'] or '-')}</div>
<div class="cards">{tarjetas}</div>
<h2>1. Plan de pruebas ({d['plan']['exitosos']} de {d['plan']['casos']} casos exitosos)</h2>
<div class="tabla"><table><tr><th>Caso</th><th>Componente</th><th>Método</th><th>Descripción</th><th>Entrada de datos</th>
<th>Tipo</th><th>Resultado esperado</th><th>Resultado real</th><th>Estado</th><th>Tiempo</th></tr>{plan}</table></div>
<h2>2. Suite completa por clase de prueba</h2>
<div class="tabla"><table><tr><th>Clase</th><th>Total</th><th>Passed</th><th>Failed</th><th>Errores</th><th>Omitidas</th><th>Tiempo</th></tr>{clases}</table></div>
<h2>3. Mensajes de error y excepciones</h2><ul>{fallas}</ul>
</main></body></html>"""


def a_markdown(d):
    t = d["totales"]
    lineas = [
        "## Informe de pruebas",
        "",
        f"| Ejecutadas | Passed | Failed | Errores | Omitidas | Plan de pruebas |",
        "|---|---|---|---|---|---|",
        f"| {t['EJECUTADAS']} | {t['PASSED']} | {t['FAILED']} | {t['ERROR']} | {t['SKIPPED']} | "
        f"{d['plan']['exitosos']}/{d['plan']['casos']} |",
        "",
        "| Caso | Componente | Tipo | Descripción | Estado |",
        "|---|---|---|---|---|",
    ]
    lineas += [f"| {f['caso']} | {f['componente']} | {f['tipo']} | {f['descripcion']} | {ICONO.get(f['estado'], f['estado'])} |"
               for f in d["plan"]["filas"]]
    if d["fallas"]:
        lineas += ["", "### Fallas", ""] + [f"- **{p['nombre']}**: {p['mensaje']}" for p in d["fallas"]]
    lineas += ["", "El informe completo (HTML y JSON) está en el artefacto **informe-pruebas** de esta ejecución."]
    return "\n".join(lineas) + "\n"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--surefire", default="backend/target/surefire-reports")
    ap.add_argument("--plan", default="docs/pruebas/plan-de-pruebas.csv")
    ap.add_argument("--salida", default="reporte-pruebas")
    a = ap.parse_args()
    os.makedirs(a.salida, exist_ok=True)
    datos = construir(leer_plan(a.plan), leer_surefire(a.surefire))
    with open(os.path.join(a.salida, "reporte-pruebas.html"), "w", encoding="utf-8") as f:
        f.write(a_html(datos))
    with open(os.path.join(a.salida, "reporte-pruebas.json"), "w", encoding="utf-8") as f:
        json.dump(datos, f, ensure_ascii=False, indent=2)
    with open(os.path.join(a.salida, "resumen.md"), "w", encoding="utf-8") as f:
        f.write(a_markdown(datos))
    t = datos["totales"]
    print(f"Ejecutadas={t['EJECUTADAS']} passed={t['PASSED']} failed={t['FAILED']} errores={t['ERROR']} "
          f"plan={datos['plan']['exitosos']}/{datos['plan']['casos']}")


if __name__ == "__main__":
    main()
