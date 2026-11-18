// Panel de RR.HH.: vacantes por estado y liberacion de asignaciones (HU-009, CU_18).

import { request } from "../api.js";
import { TEXTO_ESTADO_VACANTE } from "../catalogos.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const TEXTO_ORIGEN = {
  MANUAL: "Publicada por manager",
  RENUNCIA: "Renuncia",
  ROTACION_ACEPTADA: "Rotación aceptada",
  CAMBIO_PROYECTO: "Cambio de proyecto",
};

const usuario = montarLayout(["RRHH"]);
const mensaje = document.getElementById("mensaje");
const estado = document.getElementById("estado");
const tablaVacantes = document.getElementById("vacantes");
const tablaAsignaciones = document.getElementById("asignaciones");

async function cargarVacantes() {
  const lista = await request("GET", `/gestion/vacantes${estado.value ? `?estado=${estado.value}` : ""}`);
  tablaVacantes.innerHTML = lista.length === 0
    ? '<tr><td colspan="5" class="vacio">No hay vacantes en este estado.</td></tr>'
    : lista.map((v) => `<tr>
        <td>${escaparHtml(v.titulo)}</td>
        <td>${escaparHtml(v.area)}</td>
        <td>${TEXTO_ORIGEN[v.origen]}</td>
        <td><span class="etiqueta etiqueta-${v.estado.toLowerCase()}">${TEXTO_ESTADO_VACANTE[v.estado]}</span></td>
        <td>${v.fechaActivacion ? new Date(v.fechaActivacion).toLocaleString("es-CO") : "—"}</td>
      </tr>`).join("");
}

async function cargarAsignaciones() {
  const lista = await request("GET", "/asignaciones");
  tablaAsignaciones.innerHTML = lista.map((a) => `<tr>
      <td>${escaparHtml(a.empleado)}</td>
      <td>${escaparHtml(a.proyecto)}</td>
      <td>${escaparHtml(a.cargo)}</td>
      <td>${formatearFecha(a.fechaInicio)}</td>
      <td>
        <button type="button" class="boton" data-id="${a.id}" data-motivo="RENUNCIA">Renuncia</button>
        <button type="button" class="boton" data-id="${a.id}" data-motivo="CIERRE_PROYECTO">Cierre de proyecto</button>
      </td>
    </tr>`).join("");
}

tablaAsignaciones.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-id]");
  if (!boton) {
    return;
  }
  boton.disabled = true;
  try {
    const v = await request("POST", `/asignaciones/${boton.dataset.id}/liberar`, { motivo: boton.dataset.motivo });
    mostrarMensaje(mensaje, `Se creó la vacante "${v.titulo}" (${TEXTO_ESTADO_VACANTE[v.estado].toLowerCase()}).`, "exito");
    await Promise.all([cargarVacantes(), cargarAsignaciones()]);
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    boton.disabled = false;
  }
});

estado.addEventListener("change", () => cargarVacantes().catch((e) => mostrarMensaje(mensaje, e.message)));

if (usuario) {
  Promise.all([cargarVacantes(), cargarAsignaciones()]).catch((e) => mostrarMensaje(mensaje, e.message));
}
