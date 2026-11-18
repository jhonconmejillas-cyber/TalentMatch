// Resultado anonimo de una postulacion no seleccionada (CU_16, HU-003, RN-18).
// Muestra solo el puntaje y las habilidades de la persona seleccionada: nunca quien es.

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER", "RRHH"]);
const id = new URLSearchParams(window.location.search).get("id");
const contenedor = document.getElementById("resultado");
const mensaje = document.getElementById("mensaje");
const esPropio = usuario && usuario.rol !== "RRHH";

const si = (v) => (v ? "✔ Sí" : "✘ No");

function pintar(r) {
  const filas = r.comparacion.map((f) => {
    const brecha = f.laTieneElSeleccionado && !f.laTengoAvalada;
    const accion = brecha && esPropio
      ? (f.enMiWishlist
        ? '<span class="etiqueta etiqueta-activa">En tu wishlist</span>'
        : `<button type="button" class="boton" data-habilidad="${f.habilidadId}">Agregar a mi wishlist</button>`)
      : "";
    return `<tr class="${brecha ? "resaltada" : ""}">
      <td>${escaparHtml(f.habilidad)}</td><td>${si(f.laTengoAvalada)}</td><td>${si(f.laTieneElSeleccionado)}</td>
      <td>${accion}</td></tr>`;
  }).join("");

  contenedor.innerHTML = `
    <h1>Resultado: ${escaparHtml(r.vacante)}</h1>
    <p class="mensaje mensaje-info">
      <strong>Tu Puntaje Final: ${r.miPuntajeFinal.toFixed(1)} %</strong>
      ${r.puntajeSeleccionado != null
        ? ` · <strong>Puntaje del seleccionado: ${r.puntajeSeleccionado.toFixed(1)} %</strong>`
        : " · La vacante aún no se ha cerrado."}
      ${r.posicion ? `<br>Fuiste #${r.posicion} de ${r.totalCandidatos} candidatos.` : ""}
    </p>
    <table class="tabla">
      <thead><tr><th>Habilidad</th><th>¿La tengo avalada?</th><th>¿La tiene el seleccionado?</th><th></th></tr></thead>
      <tbody>${filas}</tbody>
    </table>
    <h2>¿Qué sigue?</h2>
    <p>Podrás volver a postularte a este puesto a partir del <strong>${formatearFecha(r.repostulacionDesde)}</strong>,
      siempre que se abra una nueva rotación en él. Desarrolla las habilidades resaltadas y pide a tu manager que las avale.</p>
    <p class="ayuda">Por privacidad no mostramos ningún dato que identifique a la persona seleccionada.</p>`;
}

async function cargar() {
  try {
    pintar(await request("GET", `/postulaciones/${id}/resultado`));
  } catch (error) {
    contenedor.innerHTML = "";
    mostrarMensaje(mensaje, error.message);
  }
}

contenedor.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-habilidad]");
  if (!boton) {
    return;
  }
  try {
    await request("POST", `/empleados/me/wishlist/${boton.dataset.habilidad}`);
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
});

if (usuario && id) {
  cargar();
}
