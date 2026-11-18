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
  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    redirigirAlPanel(usuario);
    return null;
  }
  return usuario;
}
