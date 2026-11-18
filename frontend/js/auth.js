// Sesion del usuario en el navegador: guarda el token y el usuario,
// y redirige a cada rol a su panel.

const CLAVE_TOKEN = "talentmatch.token";
const CLAVE_USUARIO = "talentmatch.usuario";

/** Panel de inicio de cada rol (dentro de pages/). */
const PANEL_POR_ROL = {
  EMPLEADO: "panel-empleado.html",
  MANAGER: "panel-manager.html",
  RRHH: "panel-rrhh.html",
  LIDER_EJECUTIVO: "panel-lider.html",
  ADMIN: "panel-admin.html",
};

export function guardarSesion(token, usuario) {
  sessionStorage.setItem(CLAVE_TOKEN, token);
  sessionStorage.setItem(CLAVE_USUARIO, JSON.stringify(usuario));
  registrarActividad();
}

/** Reemplaza el token por el renovado que devuelve la API (HU-006). */
export function renovarToken(token) {
  if (sessionStorage.getItem(CLAVE_TOKEN)) {
    sessionStorage.setItem(CLAVE_TOKEN, token);
  }
}

export function obtenerToken() {
  return sessionStorage.getItem(CLAVE_TOKEN);
}

export function usuarioActual() {
  const texto = sessionStorage.getItem(CLAVE_USUARIO);
  return texto ? JSON.parse(texto) : null;
}

export function cerrarSesion() {
  sessionStorage.removeItem(CLAVE_TOKEN);
  sessionStorage.removeItem(CLAVE_USUARIO);
  sessionStorage.removeItem(CLAVE_ACTIVIDAD);
}

// ---------------------------------------------------------------------
// Cierre por inactividad (HU-006, escenario 3). El backend es quien lo garantiza
// (el token vence a los 30 min sin peticiones); aqui solo se evita mostrar datos
// de una sesion vencida y se avisa al usuario.
// ---------------------------------------------------------------------
const CLAVE_ACTIVIDAD = "talentmatch.ultimaActividad";
const MINUTOS_INACTIVIDAD = 30; // = seguridad.minutos_inactividad_sesion en conf/config.yaml

function registrarActividad() {
  sessionStorage.setItem(CLAVE_ACTIVIDAD, String(Date.now()));
}

function sesionInactiva() {
  const ultima = Number(sessionStorage.getItem(CLAVE_ACTIVIDAD) || Date.now());
  return Date.now() - ultima > MINUTOS_INACTIVIDAD * 60 * 1000;
}

function vigilarInactividad() {
  ["click", "keydown"].forEach((e) => document.addEventListener(e, registrarActividad, { passive: true }));
  setInterval(() => {
    if (sesionInactiva()) {
      cerrarSesion();
      window.location.href = "../index.html?expirada=1";
    }
  }, 30 * 1000);
}

/** Ruta del panel del rol, relativa a la pagina actual. */
export function rutaPanel(rol) {
  const pagina = PANEL_POR_ROL[rol] || PANEL_POR_ROL.EMPLEADO;
  return window.location.pathname.includes("/pages/") ? pagina : `pages/${pagina}`;
}

export function redirigirAlPanel(usuario) {
  window.location.href = rutaPanel(usuario.rol);
}

/**
 * Protege una pagina: si no hay sesion, vuelve al login; si el rol no esta
 * permitido, lleva al panel propio. Devuelve el usuario de la sesion.
 * (La validacion real la hace el backend; esto solo evita mostrar pantallas vacias.)
 */
export function requerirSesion(rolesPermitidos) {
  const usuario = usuarioActual();
  if (!usuario || !obtenerToken()) {
    window.location.href = "../index.html";
    return null;
  }
  if (sesionInactiva()) {
    cerrarSesion();
    window.location.href = "../index.html?expirada=1";
    return null;
  }
  registrarActividad();
  vigilarInactividad();
  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    redirigirAlPanel(usuario);
    return null;
  }
  return usuario;
}
