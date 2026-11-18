// Panel de inicio de cada rol. La pagina indica su rol en <body data-rol="...">.
// Muestra accesos directos a cada accion principal: todas quedan a 1 clic (RNF-03).

import { montarLayout, nombreRol, opcionesMenu } from "../layout.js";
import { escaparHtml } from "../ui.js";

const rol = document.body.dataset.rol;
const usuario = montarLayout([rol]);
if (usuario) {
  document.getElementById("saludo").textContent = `Hola, ${usuario.nombre}`;
  document.getElementById("rol").textContent = nombreRol(usuario.rol);
  document.getElementById("accesos").innerHTML = opcionesMenu(usuario.rol)
    .filter((o) => !o.pagina.startsWith("panel-"))
    .map((o) => `<a class="tarjeta acceso" href="${o.pagina}"><strong>${escaparHtml(o.texto)}</strong></a>`)
    .join("");
}
