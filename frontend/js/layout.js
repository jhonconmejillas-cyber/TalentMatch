// Encabezado comun de las paginas con sesion: marca, menu segun el rol y cierre de sesion.

import { cerrarSesion, requerirSesion, rutaPanel } from "./auth.js";

/** Opciones del menu por rol. Cada accion principal queda a 1 clic del panel (RNF-03). */
const MENU_POR_ROL = {
  EMPLEADO: [
    { texto: "Inicio", pagina: "panel-empleado.html" },
    { texto: "Vacantes", pagina: "vacantes.html" },
    { texto: "Mis postulaciones", pagina: "mis-postulaciones.html" },
    { texto: "Mi perfil", pagina: "perfil.html" },
    { texto: "Mis habilidades", pagina: "habilidades.html" },
  ],
  MANAGER: [
    { texto: "Inicio", pagina: "panel-manager.html" },
    { texto: "Mis vacantes", pagina: "publicar-vacante.html" },
    { texto: "Avalar habilidades", pagina: "avalar.html" },
    { texto: "Mi perfil", pagina: "perfil.html" },
    { texto: "Mis habilidades", pagina: "habilidades.html" },
    { texto: "Vacantes", pagina: "vacantes.html" },
    { texto: "Mis postulaciones", pagina: "mis-postulaciones.html" },
  ],
  RRHH: [
    { texto: "Inicio", pagina: "panel-rrhh.html" },
    { texto: "Alertas", pagina: "alertas.html" },
    { texto: "Gestión de vacantes", pagina: "gestion-vacantes.html" },
    { texto: "Reporte de rechazos", pagina: "reporte-rechazos.html" },
    { texto: "Top 10", pagina: "reporte-top10.html" },
  ],
  LIDER_EJECUTIVO: [
    { texto: "Inicio", pagina: "panel-lider.html" },
    { texto: "Reporte top 10", pagina: "reporte-top10.html" },
  ],
  ADMIN: [
    { texto: "Usuarios", pagina: "panel-admin.html" },
  ],
};

// Todas las personas reciben notificaciones del sistema (RNF-08).
Object.values(MENU_POR_ROL).forEach((menu) => menu.push({ texto: "Notificaciones", pagina: "notificaciones.html" }));

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

  // El encabezado se arma con la API del DOM (textContent / href), nunca con innerHTML:
  // los datos de la sesion vienen del navegador y no deben poder inyectar HTML (XSS).
  const encabezado = crear("header", { className: "encabezado" });
  encabezado.append(crear("a", { className: "marca", href: rutaPanel(usuario.rol), textContent: "TalentMatch" }));

  const nav = crear("nav");
  nav.setAttribute("aria-label", "Menú principal");
  for (const opcion of MENU_POR_ROL[usuario.rol] || []) {
    const enlace = crear("a", { href: opcion.pagina, textContent: opcion.texto });
    if (opcion.pagina === actual) {
      enlace.setAttribute("aria-current", "page");
    }
    nav.append(enlace);
  }
  encabezado.append(nav);

  const sesion = crear("div", { className: "usuario-sesion" });
  const salir = crear("button", { type: "button", className: "boton", id: "cerrar-sesion", textContent: "Cerrar sesión" });
  sesion.append(crear("span", { textContent: `${usuario.nombre} · ${nombreRol(usuario.rol)}` }), salir);
  encabezado.append(sesion);

  document.body.prepend(encabezado);
  salir.addEventListener("click", () => {
    cerrarSesion();
    window.location.href = "../index.html";
  });
  return usuario;
}

/** Crea un elemento y le asigna propiedades (textContent, href, className...). */
function crear(etiqueta, propiedades = {}) {
  return Object.assign(document.createElement(etiqueta), propiedades);
}

/** Opciones del menu de un rol (el panel de inicio las muestra como accesos directos). */
export function opcionesMenu(rol) {
  return [...(MENU_POR_ROL[rol] || [])];
}
