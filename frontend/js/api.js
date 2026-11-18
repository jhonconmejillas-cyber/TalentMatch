// Cliente HTTP UNICO hacia el backend. Ninguna pagina llama a fetch directamente:
// todas usan request(), que agrega el token, convierte a JSON y traduce los errores.

import { API_URL } from "./config.js";
import { obtenerToken, cerrarSesion, renovarToken } from "./auth.js";

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

// HU-005 (escenario 3): si una respuesta tarda mas de 300 ms se muestra un indicador
// de carga para que el usuario no piense que la aplicacion se congelo.
const MS_INDICADOR_CARGA = 300;
let pendientes = 0;
let temporizadorCarga = null;

function inicioCarga() {
  pendientes += 1;
  if (pendientes === 1) {
    temporizadorCarga = setTimeout(() => {
      let barra = document.getElementById("indicador-carga");
      if (!barra) {
        barra = document.createElement("div");
        barra.id = "indicador-carga";
        barra.className = "indicador-carga";
        barra.setAttribute("role", "progressbar");
        barra.setAttribute("aria-label", "Cargando");
        document.body.appendChild(barra);
      }
      barra.hidden = false;
    }, MS_INDICADOR_CARGA);
  }
}

function finCarga() {
  pendientes = Math.max(0, pendientes - 1);
  if (pendientes === 0) {
    clearTimeout(temporizadorCarga);
    const barra = document.getElementById("indicador-carga");
    if (barra) {
      barra.hidden = true;
    }
  }
}

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
  let texto;
  inicioCarga();
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, opciones);
    texto = await respuesta.text();
  } catch (e) {
    throw new ErrorApi(MENSAJES_POR_ESTADO[0], 0);
  } finally {
    finCarga();
  }

  const cuerpo = texto ? intentarJson(texto) : null;

  // HU-006: la API devuelve un token nuevo en cada peticion; la sesion vence tras 30 min SIN actividad.
  const renovado = respuesta.headers.get("X-Token-Renovado");
  if (renovado) {
    renovarToken(renovado);
  }

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

/** Descarga un archivo protegido (con el token) y lo guarda con el nombre indicado. */
export async function descargar(ruta, nombreArchivo) {
  const respuesta = await fetch(`${API_URL}${ruta}`, { headers: { Authorization: `Bearer ${obtenerToken()}` } });
  if (!respuesta.ok) {
    const cuerpo = intentarJson(await respuesta.text());
    throw new ErrorApi((cuerpo && cuerpo.mensaje) || MENSAJES_POR_ESTADO[respuesta.status] || MENSAJES_POR_ESTADO[500],
      respuesta.status);
  }
  const url = URL.createObjectURL(await respuesta.blob());
  const enlace = document.createElement("a");
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(url);
}
