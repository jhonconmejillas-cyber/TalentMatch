// Registro de una cuenta nueva (CU_01, RF-01). Al terminar, inicia sesion y va al panel.

import { request } from "../api.js";
import { guardarSesion, redirigirAlPanel } from "../auth.js";
import { limpiarCampos, limpiarMensaje, marcarCampo, marcarErroresDeApi, mostrarMensaje } from "../ui.js";

const formulario = document.getElementById("form-registro");
const mensaje = document.getElementById("mensaje");

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarMensaje(mensaje);
  limpiarCampos(formulario);

  const datos = {
    nombre: formulario.nombre.value.trim(),
    correo: formulario.correo.value.trim(),
    contrasena: formulario.contrasena.value,
  };

  let valido = true;
  if (!datos.nombre) {
    marcarCampo(formulario.nombre, "Escribe tu nombre completo.");
    valido = false;
  }
  if (!datos.correo || !formulario.correo.checkValidity()) {
    marcarCampo(formulario.correo, "Escribe un correo válido (ejemplo: nombre@empresa.com).");
    valido = false;
  }
  if (datos.contrasena !== formulario.confirmacion.value) {
    marcarCampo(formulario.confirmacion, "Las contraseñas no coinciden.");
    valido = false;
  }
  if (!valido) {
    return;
  }

  const boton = formulario.querySelector("button[type=submit]");
  boton.disabled = true;
  try {
    await request("POST", "/auth/registro", datos);
    const sesion = await request("POST", "/auth/login", { correo: datos.correo, contrasena: datos.contrasena });
    guardarSesion(sesion.token, sesion.usuario);
    redirigirAlPanel(sesion.usuario);
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    marcarErroresDeApi(formulario, error.detalles);
  } finally {
    boton.disabled = false;
  }
});
