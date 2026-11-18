// Gestion de usuarios y roles del Administrador (CU_23, RF-03).

import { request } from "../api.js";
import { montarLayout, nombreRol } from "../layout.js";
import { ejecutar, escaparHtml, mostrarMensaje } from "../ui.js";

const ROLES = ["EMPLEADO", "MANAGER", "RRHH", "LIDER_EJECUTIVO", "ADMIN"];
const usuario = montarLayout(["ADMIN"]);
const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");

async function cargar() {
  try {
    const usuarios = await request("GET", "/usuarios");
    filas.innerHTML = usuarios.map(fila).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

function fila(u) {
  const opciones = ROLES
    .map((r) => `<option value="${r}"${r === u.rol ? " selected" : ""}>${nombreRol(r)}</option>`)
    .join("");
  const propio = u.id === usuario.id;
  return `<tr data-id="${u.id}">
    <td>${escaparHtml(u.nombre)}</td>
    <td>${escaparHtml(u.correo)}</td>
    <td><select class="cambiar-rol" aria-label="Rol de ${escaparHtml(u.nombre)}"${propio ? " disabled" : ""}>${opciones}</select></td>
    <td><span class="etiqueta ${u.activo ? "etiqueta-activa" : "etiqueta-cerrada"}">${u.activo ? "Activa" : "Desactivada"}</span></td>
    <td>${propio ? "" : `<button type="button" class="boton cambiar-estado" data-activo="${u.activo}">
      ${u.activo ? "Desactivar" : "Activar"}</button>`}</td>
  </tr>`;
}

async function actualizar(id, cambios, texto) {
  try {
    await request("PATCH", `/usuarios/${id}`, cambios);
    mostrarMensaje(mensaje, texto, "exito");
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

filas.addEventListener("change", (e) => {
  if (e.target.classList.contains("cambiar-rol")) {
    const id = e.target.closest("tr").dataset.id;
    ejecutar(actualizar(id, { rol: e.target.value }, "Rol actualizado."));
  }
});

filas.addEventListener("click", (e) => {
  if (e.target.classList.contains("cambiar-estado")) {
    const id = e.target.closest("tr").dataset.id;
    const activo = e.target.dataset.activo === "true";
    ejecutar(actualizar(id, { activo: !activo }, activo ? "Cuenta desactivada." : "Cuenta activada."));
  }
});

if (usuario) {
  ejecutar(cargar());
}
