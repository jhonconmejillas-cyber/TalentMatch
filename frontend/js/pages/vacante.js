// Detalle de una vacante (CU_10) con el Puntaje Final del empleado y su desglose (CU_11, RF-16, RF-17).

import { request } from "../api.js";
import { NOMBRE_SENIORIDAD } from "../catalogos.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER", "RRHH"]);
const id = new URLSearchParams(window.location.search).get("id");
const detalle = document.getElementById("detalle");
const acciones = document.getElementById("acciones");
const mensaje = document.getElementById("mensaje");

const chips = (lista) => lista.map((h) => `<span class="etiqueta">${escaparHtml(h.nombre ?? h)}</span>`).join(" ") || "—";
const porcentaje = (x) => `${(x * 100).toFixed(0)} %`;

function pintarDetalle(v) {
  detalle.innerHTML = `
    <h1>${escaparHtml(v.titulo)}</h1>
    <p class="ayuda">${escaparHtml(v.area)} · ${NOMBRE_SENIORIDAD[v.senioridad]}
      ${v.proyecto ? ` · ${escaparHtml(v.proyecto.nombre)}` : ""}
      ${v.manager ? ` · Manager: ${escaparHtml(v.manager)}` : ""} · publicada el ${formatearFecha(v.fechaActivacion)}</p>
    ${v.descripcion ? `<p>${escaparHtml(v.descripcion)}</p>` : ""}
    <p><strong>Habilidades críticas:</strong> ${chips(v.habilidadesCriticas)}</p>
    <p><strong>Habilidades deseables:</strong> ${chips(v.habilidadesDeseables)}</p>`;
}

/** Tarjeta con el Puntaje Final y el desglose explicable (sin IA). */
function pintarPuntaje(p) {
  const d = p.desglose;
  const seccion = document.createElement("section");
  seccion.className = "tarjeta";
  seccion.id = "mi-puntaje";
  seccion.innerHTML = `
    <h2>Tu Puntaje Final: ${p.puntajeFinal.toFixed(1)} %</h2>
    ${p.aviso ? `<p class="mensaje mensaje-alerta">${escaparHtml(p.aviso)}</p>`
      : `<p class="mensaje mensaje-exito">Eres un candidato viable (umbral ${p.umbralViabilidad.toFixed(0)} %).</p>`}
    <details>
      <summary>¿Cómo se calculó?</summary>
      <p>Puntaje Final = Match Score =
        (habilidades ${porcentaje(d.porcentajeHabilidades)} × ${d.pesoHabilidades})
        + (ajuste por senioridad ${d.ajusteSenioridad} × ${d.pesoSenioridad})</p>
      <p><strong>Habilidades avaladas que coinciden:</strong> ${chips(d.habilidadesCoincidentes)}</p>
      <p><strong>Habilidades que te faltan:</strong> ${chips(d.habilidadesFaltantes)}</p>
      ${d.habilidadesSinAvalar.length ? `<p class="ayuda">Tienes ${chips(d.habilidadesSinAvalar)} en tu perfil,
        pero aún sin avalar por tu manager: no cuentan para el cálculo.</p>` : ""}
      <p class="ayuda">Solo cuentan tus habilidades avaladas y tu senioridad. Ningún dato personal interviene.</p>
    </details>`;
  acciones.prepend(seccion);
}

async function iniciar() {
  try {
    const vacante = await request("GET", `/vacantes/${id}`);
    pintarDetalle(vacante);
    if (["EMPLEADO", "MANAGER"].includes(usuario.rol) && vacante.estado === "ACTIVA") {
      pintarPuntaje(await request("GET", `/vacantes/${id}/mi-puntaje`));
    }
    document.dispatchEvent(new CustomEvent("vacante-cargada", { detail: vacante }));
  } catch (error) {
    detalle.innerHTML = "";
    mostrarMensaje(mensaje, error.message);
  }
}

if (usuario && id) {
  iniciar();
}
