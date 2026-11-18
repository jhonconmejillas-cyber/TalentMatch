// Encabezado comun de las paginas con sesion: marca, menu segun el rol y cierre de sesion.

import { cerrarSesion, requerirSesion, rutaPanel } from "./auth.js";
import { escaparHtml } from "./ui.js";

/** Opciones del menu por rol. Cada accion principal queda a 1 clic del panel (RNF-03). */
const MENU_POR_ROL = {
  EMPLEADO: [
    { texto: "Inicio", pagina: "panel-empleado.html" },
  ],
  MANAGER: [
    { texto: "Inicio", pagina: "panel-manager.html" },
  ],
  RRHH: [
    { texto: "Inicio", pagina: "panel-rrhh.html" },
  ],
  LIDER_EJECUTIVO: [
    { texto: "Inicio", pagina: "panel-lider.html" },
  ],
  ADMIN: [
    { texto: "Usuarios", pagina: "panel-admin.html" },
  ],
};

const NOMBRE_ROL = {
  EMPLEADO: "Empleado",
  MANAGER: "Manager",
  RRHH: "RR.HH.",
  LIDER_EJECUTIVO: "Líder Ejecutivo",
  ADMIN: "Administrador",
};

export function nombreRol(rol) {
  return NOMBRE_ROL[rol] || rol;
}

/**
 * Protege la pagina y dibuja el encabezado. Devuelve el usuario de la sesion
 * (o null si se redirigio al login).
 * @param {string[]} [rolesPermitidos] roles que pueden ver la pagina
 */
export function montarLayout(rolesPermitidos) {
  const usuario = requerirSesion(rolesPermitidos);
  if (!usuario) {
    return null;
  }
  const actual = window.location.pathname.split("/").pop();
  const opciones = (MENU_POR_ROL[usuario.rol] || [])
    .map((o) => `<a href="${o.pagina}"${o.pagina === actual ? ' aria-current="page"' : ""}>${escaparHtml(o.texto)}</a>`)
    .join("");

  const encabezado = document.createElement("header");
  encabezado.className = "encabezado";
  encabezado.innerHTML = `
    <a class="marca" href="${rutaPanel(usuario.rol)}">TalentMatch</a>
    <nav aria-label="Menú principal">${opciones}</nav>
    <div class="usuario-sesion">
      <span>${escaparHtml(usuario.nombre)} · ${escaparHtml(nombreRol(usuario.rol))}</span>
      <button type="button" class="boton" id="cerrar-sesion">Cerrar sesión</button>
    </div>`;
  document.body.prepend(encabezado);
  document.getElementById("cerrar-sesion").addEventListener("click", () => {
    cerrarSesion();
    window.location.href = "../index.html";
  });
  return usuario;
}

/** Agrega opciones al menu de un rol (lo usan las paginas que se van sumando). */
export function registrarOpcionesMenu(rol, opciones) {
  MENU_POR_ROL[rol] = [...(MENU_POR_ROL[rol] || []), ...opciones];
}
