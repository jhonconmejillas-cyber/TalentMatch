// Cliente HTTP UNICO hacia el backend. Ninguna pagina llama a fetch directamente:
// todas usan request(), que agrega el token, convierte a JSON y traduce los errores.

import { API_URL } from "./config.js";
import { obtenerToken, cerrarSesion } from "./auth.js";

/** Error con el mensaje en espanol que devolvio la API (RNF-13). */
export class ErrorApi extends Error {
  constructor(mensaje, estado, detalles) {
    super(mensaje);
    this.estado = estado;
    this.detalles = detalles || {};
  }
}

const MENSAJES_POR_ESTADO = {
  0: "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.",
  400: "Los datos enviados no son válidos.",
  401: "Tu sesión expiró. Inicia sesión de nuevo.",
  403: "No tienes permiso para realizar esta acción.",
  404: "No se encontró el recurso solicitado.",
  409: "La operación no se puede realizar en el estado actual.",
  500: "Ocurrió un error en el servidor. Inténtalo mas tarde.",
};

/**
 * Llama a la API.
 * @param {string} metodo  GET, POST, PUT, PATCH o DELETE
 * @param {string} ruta    ruta relativa a /api/v1, por ejemplo "/auth/login"
 * @param {object} [datos] cuerpo JSON (opcional)
 * @returns {Promise<any>} la respuesta ya convertida desde JSON (o null si no hay cuerpo)
 */
export async function request(metodo, ruta, datos) {
  const encabezados = { Accept: "application/json" };
  const token = obtenerToken();
  if (token) {
    encabezados.Authorization = `Bearer ${token}`;
  }
  const opciones = { method: metodo, headers: encabezados };
  if (datos !== undefined) {
    encabezados["Content-Type"] = "application/json";
    opciones.body = JSON.stringify(datos);
  }

  let respuesta;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, opciones);
  } catch (e) {
    throw new ErrorApi(MENSAJES_POR_ESTADO[0], 0);
  }

  const texto = await respuesta.text();
  const cuerpo = texto ? intentarJson(texto) : null;

  if (!respuesta.ok) {
    // Token vencido o invalido: se cierra la sesion y se vuelve al login.
    if (respuesta.status === 401 && token) {
      cerrarSesion();
      window.location.href = rutaLogin();
    }
    const mensaje = (cuerpo && cuerpo.mensaje) || MENSAJES_POR_ESTADO[respuesta.status]
      || MENSAJES_POR_ESTADO[500];
    throw new ErrorApi(mensaje, respuesta.status, cuerpo && cuerpo.campos);
  }
  return cuerpo;
}

function intentarJson(texto) {
  try {
    return JSON.parse(texto);
  } catch (e) {
    return null;
  }
}

/** Ruta al login desde cualquier pagina (raiz o pages/). */
export function rutaLogin() {
  return window.location.pathname.includes("/pages/") ? "../index.html" : "index.html";
}
