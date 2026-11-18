// Perfil profesional del empleado (CU_03, RF-04).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, limpiarCampos, limpiarMensaje, marcarErroresDeApi, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER"]);
const formulario = document.getElementById("form-perfil");
const mensaje = document.getElementById("mensaje");

function bloquearSiTieneValor(input) {
  input.readOnly = Boolean(input.value);
  input.disabled = Boolean(input.value) && input.tagName === "SELECT";
}

function pintar(perfil) {
  formulario.senioridad.value = perfil.senioridad || "";
  formulario.aniosExperiencia.value = perfil.aniosExperiencia ?? 0;
  formulario.area.value = perfil.area || "";
  formulario.fechaIngresoEmpresa.value = perfil.fechaIngresoEmpresa || "";
  formulario.fechaInicioRol.value = perfil.fechaInicioRol || "";
  formulario.proyectoActualId.value = perfil.proyectoActual ? perfil.proyectoActual.id : "";
  formulario.cargo.value = perfil.cargo || "";
  // Datos que, una vez declarados, solo cambian por RR.HH. o por una rotacion.
  [formulario.fechaIngresoEmpresa, formulario.fechaInicioRol, formulario.proyectoActualId, formulario.cargo]
    .forEach(bloquearSiTieneValor);
  document.getElementById("resumen").textContent =
    `${perfil.nombre} · ${perfil.correo}` + (perfil.manager ? ` · Manager: ${perfil.manager}` : "");
}

async function cargar() {
  try {
    const [perfil, proyectos] = await Promise.all([
      request("GET", "/empleados/me/perfil"),
      request("GET", "/proyectos"),
    ]);
    formulario.proyectoActualId.insertAdjacentHTML("beforeend", proyectos
      .map((p) => `<option value="${p.id}">${escaparHtml(p.nombre)} (${escaparHtml(p.area)})</option>`).join(""));
    pintar(perfil);
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarMensaje(mensaje);
  limpiarCampos(formulario);
  const datos = {
    senioridad: formulario.senioridad.value || null,
    aniosExperiencia: formulario.aniosExperiencia.value === "" ? null : Number(formulario.aniosExperiencia.value),
    area: formulario.area.value.trim(),
    fechaIngresoEmpresa: formulario.fechaIngresoEmpresa.value || null,
    fechaInicioRol: formulario.fechaInicioRol.value || null,
    proyectoActualId: formulario.proyectoActualId.value ? Number(formulario.proyectoActualId.value) : null,
    cargo: formulario.cargo.value.trim() || null,
  };
  try {
    const perfil = await request("PUT", "/empleados/me/perfil", datos);
    pintar(perfil);
    mostrarMensaje(mensaje, "Tu perfil se guardó correctamente.", "exito");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    marcarErroresDeApi(formulario, error.detalles);
  }
});

if (usuario) {
  ejecutar(cargar());
}
