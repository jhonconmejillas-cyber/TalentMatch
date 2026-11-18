// Bandeja de alertas de RR.HH. (CU_24, RF-32, RF-14).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, mostrarMensaje } from "../ui.js";

const TIPO = { RECHAZOS_REPETIDOS: "Rechazos repetidos", VACANTE_SIN_MATCH: "Vacante sin match" };
const usuario = montarLayout(["RRHH"]);
const lista = document.getElementById("lista");
const mensaje = document.getElementById("mensaje");
const verResueltas = document.getElementById("ver-resueltas");

async function cargar() {
  try {
    const alertas = await request("GET", verResueltas.checked ? "/alertas" : "/alertas?estado=ABIERTA");
    lista.innerHTML = alertas.length === 0
      ? '<p class="vacio">No hay alertas abiertas.</p>'
      : alertas.map((a) => `
        <article class="tarjeta">
          <div class="titulo-seccion">
            <h3>${TIPO[a.tipo]}${a.manager ? ` · Manager ID ${a.managerId}` : ""}</h3>
            <span class="etiqueta ${a.estado === "ABIERTA" ? "etiqueta-agregado" : "etiqueta-activa"}">
              ${a.estado === "ABIERTA" ? "Abierta" : "Resuelta"}</span>
          </div>
          <p>${escaparHtml(a.detalle)}</p>
          <p class="ayuda">${new Date(a.fecha).toLocaleString("es-CO")}
            ${a.vacanteId ? ` · <a href="ranking.html?id=${a.vacanteId}">Ver ranking de "${escaparHtml(a.vacante)}"</a>` : ""}</p>
          ${a.estado === "ABIERTA"
            ? `<button type="button" class="boton" data-id="${a.id}">Marcar como revisada</button>` : ""}
        </article>`).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

lista.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-id]");
  if (!boton) {
    return;
  }
  try {
    await request("PATCH", `/alertas/${boton.dataset.id}`);
    mostrarMensaje(mensaje, "Alerta marcada como revisada.", "exito");
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
});
verResueltas.addEventListener("change", cargar);

if (usuario) {
  cargar();
}
