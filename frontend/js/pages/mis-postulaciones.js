// Historial de postulaciones del empleado (CU_21, RF-31).

import { request } from "../api.js";
import { TEXTO_ESTADO_POSTULACION } from "../catalogos.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER"]);
const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");

function posicion(p) {
  return p.posicionRanking ? `Fuiste #${p.posicionRanking} de ${p.totalCandidatos} candidatos` : "";
}

function fila(p) {
  const resultado = p.estado === "RECHAZADA"
    ? `<a href="resultado.html?id=${p.id}">Ver resultado</a>` : "";
  return `<tr>
    <td><a href="vacante.html?id=${p.vacanteId}">${escaparHtml(p.vacante)}</a></td>
    <td>${escaparHtml(p.area)}</td>
    <td>${p.puntajeFinal.toFixed(1)} %</td>
    <td>${formatearFecha(p.fechaCreacion)}</td>
    <td><span class="etiqueta etiqueta-${p.estado.toLowerCase()}">${TEXTO_ESTADO_POSTULACION[p.estado]}</span>
      <div class="ayuda">${posicion(p)}</div>
      ${p.mensaje ? `<div class="ayuda">${escaparHtml(p.mensaje)}</div>` : ""}</td>
    <td>${resultado}</td>
  </tr>`;
}

async function cargar() {
  try {
    const lista = await request("GET", "/empleados/me/postulaciones");
    filas.innerHTML = lista.length === 0
      ? '<tr><td colspan="6" class="vacio">Aún no te has postulado. <a href="vacantes.html">Explora las vacantes</a>.</td></tr>'
      : lista.map(fila).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

if (usuario) {
  cargar();
}
