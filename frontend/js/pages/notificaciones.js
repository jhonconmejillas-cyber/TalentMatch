// Notificaciones in-app del usuario (RNF-08).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, mostrarMensaje } from "../ui.js";

const usuario = montarLayout();
const lista = document.getElementById("lista");
const mensaje = document.getElementById("mensaje");

function enlace(n) {
  if (n.tipo === "RESULTADO_POSTULACION" && n.postulacionId) {
    return `<a href="resultado.html?id=${n.postulacionId}">Ver resultado</a>`;
  }
  if (n.vacanteId && ["VACANTE_WISHLIST"].includes(n.tipo)) {
    return `<a href="vacante.html?id=${n.vacanteId}">Ver vacante</a>`;
  }
  return "";
}

async function cargar() {
  try {
    const notificaciones = await request("GET", "/notificaciones");
    lista.innerHTML = notificaciones.length === 0
      ? '<p class="vacio">No tienes notificaciones.</p>'
      : notificaciones.map((n) => `
        <article class="tarjeta" style="${n.leida ? "opacity:.7" : "border-left:4px solid var(--color-primario)"}">
          <h3>${escaparHtml(n.titulo)}</h3>
          <p>${escaparHtml(n.mensaje)}</p>
          <p class="ayuda">${new Date(n.fecha).toLocaleString("es-CO")} ${enlace(n)}
            ${n.leida ? "" : `· <button type="button" class="boton-enlace" data-id="${n.id}">Marcar como leída</button>`}</p>
        </article>`).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

lista.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-id]");
  if (boton) {
    await request("PATCH", `/notificaciones/${boton.dataset.id}`);
    await cargar();
  }
});

if (usuario) {
  ejecutar(cargar());
}
