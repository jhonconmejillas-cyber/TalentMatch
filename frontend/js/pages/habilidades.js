// Habilidades del empleado: agregar (RF-06) y ver su estado de aval (RN-19).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, limpiarCampos, marcarErroresDeApi, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER"]);
const formulario = document.getElementById("form-habilidad");
const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");

const TEXTO_ESTADO = { AGREGADO: "Pendiente de aval", AVALADO: "Avalada", RECHAZADO: "No avalada" };

async function cargar() {
  const lista = await request("GET", "/empleados/me/habilidades");
  filas.innerHTML = lista.length === 0
    ? '<tr><td colspan="4" class="vacio">Aún no tienes habilidades en tu perfil.</td></tr>'
    : lista.map((h) => `<tr>
        <td>${escaparHtml(h.habilidad)}</td>
        <td>${escaparHtml(h.nivelDeclarado || "—")}</td>
        <td>${formatearFecha(h.fechaAgregado)}</td>
        <td><span class="etiqueta etiqueta-${h.estado.toLowerCase()}">${TEXTO_ESTADO[h.estado]}</span></td>
      </tr>`).join("");
}

async function iniciar() {
  try {
    const catalogo = await request("GET", "/habilidades");
    formulario.habilidadId.insertAdjacentHTML("beforeend",
      catalogo.map((h) => `<option value="${h.id}">${escaparHtml(h.nombre)}</option>`).join(""));
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarCampos(formulario);
  if (!formulario.habilidadId.value) {
    mostrarMensaje(mensaje, "Selecciona una habilidad del catálogo.");
    return;
  }
  try {
    const nueva = await request("POST", "/empleados/me/habilidades", {
      habilidadId: Number(formulario.habilidadId.value),
      nivelDeclarado: formulario.nivelDeclarado.value || null,
    });
    mostrarMensaje(mensaje, `${nueva.habilidad} quedó agregada. Tu manager debe avalarla para que cuente.`, "exito");
    formulario.reset();
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    marcarErroresDeApi(formulario, error.detalles);
  }
});

if (usuario) {
  iniciar();
}
