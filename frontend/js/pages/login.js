// Pagina de inicio de sesion (CU_02, RF-02).

import { request } from "../api.js";
import { guardarSesion, redirigirAlPanel, usuarioActual } from "../auth.js";
import { limpiarCampos, limpiarMensaje, marcarCampo, mostrarMensaje } from "../ui.js";

const formulario = document.getElementById("form-login");
const mensaje = document.getElementById("mensaje");

// Aviso cuando la sesion se cerro por inactividad (HU-006).
if (new URLSearchParams(window.location.search).has("expirada")) {
  mostrarMensaje(mensaje, "Tu sesión se cerró tras 30 minutos de inactividad. Inicia sesión de nuevo.", "info");
}

// Si ya hay una sesion abierta, ir directo al panel.
const sesion = usuarioActual();
if (sesion) {
  redirigirAlPanel(sesion);
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarMensaje(mensaje);
  limpiarCampos(formulario);

  const correo = formulario.correo.value.trim();
  const contrasena = formulario.contrasena.value;

  // Validacion basica de formato; las reglas reales las aplica el backend.
  let valido = true;
  if (!correo) {
    marcarCampo(formulario.correo, "Escribe tu correo corporativo.");
    valido = false;
  } else if (!formulario.correo.checkValidity()) {
    marcarCampo(formulario.correo, "El correo no tiene un formato válido (ejemplo: nombre@empresa.com).");
    valido = false;
  }
  if (!contrasena) {
    marcarCampo(formulario.contrasena, "Escribe tu contraseña.");
    valido = false;
  }
  if (!valido) {
    return;
  }

  const boton = formulario.querySelector("button[type=submit]");
  boton.disabled = true;
  try {
    const respuesta = await request("POST", "/auth/login", { correo, contrasena });
    guardarSesion(respuesta.token, respuesta.usuario);
    redirigirAlPanel(respuesta.usuario);
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  } finally {
    boton.disabled = false;
  }
});
