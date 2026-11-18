// Panel de inicio de cada rol. La pagina indica su rol en <body data-rol="...">
// y las tarjetas de accesos directos en <section id="accesos">.

import { montarLayout, nombreRol } from "../layout.js";

const rol = document.body.dataset.rol;
const usuario = montarLayout([rol]);
if (usuario) {
  document.getElementById("saludo").textContent = `Hola, ${usuario.nombre}`;
  document.getElementById("rol").textContent = nombreRol(usuario.rol);
}
