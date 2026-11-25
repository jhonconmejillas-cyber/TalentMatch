// Habilidades del empleado: agregar (RF-06) y ver su estado de aval (RN-19).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, formatearFecha, limpiarCampos, marcarErroresDeApi, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER"]);
const formulario = document.getElementById("form-habilidad");
const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");

const TEXTO_ESTADO = { AGREGADO: "Pendiente de aval", AVALADO: "Avalada", RECHAZADO: "No avalada" };

async function cargar() {
  const lista = await request("GET", "/empleados/me/habilidades");
  filas.innerHTML = lista.length === 0
    ? '<tr><td colspan="4" class="vacio">Aún no tienes habilidades en tu perfil.</td></tr>'
    : lista.map((h) => `<tr>
        <td>${escaparHtml(h.habilidad)}</td>
        <td>${escaparHtml(h.nivelDeclarado || "—")}</td>
        <td>${formatearFecha(h.fechaAgregado)}</td>
        <td><span class="etiqueta etiqueta-${h.estado.toLowerCase()}">${TEXTO_ESTADO[h.estado]}</span></td>
      </tr>`).join("");
}

const formWishlist = document.getElementById("form-wishlist");
const wishlist = document.getElementById("wishlist");

function pintarWishlist(lista) {
  wishlist.innerHTML = lista.length === 0 ? '<span class="ayuda">Tu wishlist está vacía.</span>'
    : lista.map((h) => `<span class="etiqueta">${escaparHtml(h.nombre)}
        <button type="button" class="boton-enlace" data-quitar="${h.id}" aria-label="Quitar ${escaparHtml(h.nombre)}">✕</button>
      </span>`).join(" ");
}

formWishlist.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (formWishlist.wishlistId.value) {
    pintarWishlist(await request("POST", `/empleados/me/wishlist/${formWishlist.wishlistId.value}`));
  }
});

wishlist.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-quitar]");
  if (boton) {
    pintarWishlist(await request("DELETE", `/empleados/me/wishlist/${boton.dataset.quitar}`));
  }
});

async function iniciar() {
  try {
    const catalogo = await request("GET", "/habilidades");
    const opciones = catalogo.map((h) => `<option value="${h.id}">${escaparHtml(h.nombre)}</option>`).join("");
    formulario.habilidadId.insertAdjacentHTML("beforeend", opciones);
    formWishlist.wishlistId.insertAdjacentHTML("beforeend", opciones);
    pintarWishlist(await request("GET", "/empleados/me/wishlist"));
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarCampos(formulario);
  if (!formulario.habilidadId.value) {
    mostrarMensaje(mensaje, "Selecciona una habilidad del catálogo.");
    return;
  }
  try {
    const nueva = await request("POST", "/empleados/me/habilidades", {
      habilidadId: Number(formulario.habilidadId.value),
      nivelDeclarado: formulario.nivelDeclarado.value || null,
    });
    mostrarMensaje(mensaje, `${nueva.habilidad} quedó agregada. Tu manager debe avalarla para que cuente.`, "exito");
    formulario.reset();
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    marcarErroresDeApi(formulario, error.detalles);
  }
});

if (usuario) {
  ejecutar(iniciar());
}
