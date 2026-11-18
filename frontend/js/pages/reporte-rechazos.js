// Reporte mensual de patrones de rechazo para RR.HH. (HU-002).

import { descargar, request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["RRHH"]);
const periodo = document.getElementById("periodo");
const filas = document.getElementById("filas");
const detalle = document.getElementById("detalle");
const mensaje = document.getElementById("mensaje");
let reporte;

const pct = (v) => `${v.toFixed(1)} %`;

function mesAnterior() {
  const d = new Date();
  d.setDate(1);
  d.setMonth(d.getMonth() - 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

async function cargar() {
  try {
    reporte = await request("GET", `/reportes/rechazos?periodo=${periodo.value}`);
    document.getElementById("resumen").textContent =
      `Ventana: ${reporte.desde.slice(0, 10)} a ${reporte.hasta.slice(0, 10)} · Promedio de la organización `
      + `(rechazo a candidatos ≥ ${reporte.umbralViabilidad} %): ${pct(reporte.promedioTasaRechazoViables)} · `
      + `Se resalta una desviación mayor a ${reporte.desviacionResaltadoPct} puntos.`;
    filas.innerHTML = reporte.managers.length === 0
      ? '<tr><td colspan="8" class="vacio">No hubo decisiones en este periodo.</td></tr>'
      : reporte.managers.map((m, i) => `<tr class="${m.resaltado ? "resaltada" : ""}">
          <td>${m.managerId}</td><td>${m.decisiones}</td><td>${pct(m.tasaAceptacion)}</td><td>${pct(m.tasaRechazo)}</td>
          <td><strong>${pct(m.tasaRechazoViables)}</strong> (${m.rechazosViables} de ${m.candidatosViables})</td>
          <td>${m.desviacion > 0 ? "+" : ""}${m.desviacion.toFixed(1)}</td>
          <td>${m.alertasRelacionadas.map((a) => `<a href="alertas.html">#${a}</a>`).join(" ") || "—"}</td>
          <td>${m.detalle.length ? `<button type="button" class="boton-enlace" data-i="${i}">Ver detalle</button>` : ""}</td>
        </tr>`).join("");
    detalle.hidden = true;
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

filas.addEventListener("click", (e) => {
  const boton = e.target.closest("button[data-i]");
  if (!boton) {
    return;
  }
  const m = reporte.managers[Number(boton.dataset.i)];
  detalle.hidden = false;
  detalle.innerHTML = `<h2>Rechazos a candidatos viables · Manager ID ${m.managerId}</h2>
    <table class="tabla"><thead><tr><th>Candidato</th><th>Puntaje Final</th><th>Puntaje del seleccionado</th>
      <th>Vacante</th><th>Fecha</th></tr></thead><tbody>
    ${m.detalle.map((d) => `<tr><td>${escaparHtml(d.candidato)}</td><td>${pct(d.puntajeFinal)}</td>
      <td>${d.puntajeSeleccionado == null ? "Sin seleccionado" : pct(d.puntajeSeleccionado)}</td>
      <td>${escaparHtml(d.vacante)}</td><td>${d.fecha.slice(0, 10)}</td></tr>`).join("")}
    </tbody></table>`;
  detalle.scrollIntoView({ behavior: "smooth" });
});

document.getElementById("descargar").addEventListener("click", () =>
  descargar(`/reportes/rechazos/archivo?periodo=${periodo.value}`, `reporte-rechazos-${periodo.value}.csv`)
    .catch((e) => mostrarMensaje(mensaje, e.message)));
periodo.addEventListener("change", cargar);

if (usuario) {
  periodo.value = mesAnterior();
  cargar();
}
