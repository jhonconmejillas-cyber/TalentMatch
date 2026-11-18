// Detalle de una vacante. Las secciones de puntaje y postulacion se agregan en
// las paginas de matching (mi puntaje) y postulacion.

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";
import { NOMBRE_SENIORIDAD } from "../catalogos.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER", "RRHH"]);
const id = new URLSearchParams(window.location.search).get("id");
const detalle = document.getElementById("detalle");
const mensaje = document.getElementById("mensaje");

export function pintarDetalle(v) {
  const chips = (lista) => lista.map((h) => `<span class="etiqueta">${escaparHtml(h.nombre)}</span>`).join(" ") || "—";
  detalle.innerHTML = `
    <h1>${escaparHtml(v.titulo)}</h1>
    <p class="ayuda">${escaparHtml(v.area)} · ${NOMBRE_SENIORIDAD[v.senioridad]}
      ${v.proyecto ? ` · ${escaparHtml(v.proyecto.nombre)}` : ""}
      ${v.manager ? ` · Manager: ${escaparHtml(v.manager)}` : ""} · publicada el ${formatearFecha(v.fechaActivacion)}</p>
    ${v.descripcion ? `<p>${escaparHtml(v.descripcion)}</p>` : ""}
    <p><strong>Habilidades críticas:</strong> ${chips(v.habilidadesCriticas)}</p>
    <p><strong>Habilidades deseables:</strong> ${chips(v.habilidadesDeseables)}</p>`;
}

export const vacanteCargada = (async () => {
  if (!usuario || !id) {
    return null;
  }
  try {
    const v = await request("GET", `/vacantes/${id}`);
    pintarDetalle(v);
    return { vacante: v, usuario };
  } catch (error) {
    detalle.innerHTML = "";
    mostrarMensaje(mensaje, error.message);
    return null;
  }
})();
