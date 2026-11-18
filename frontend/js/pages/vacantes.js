// Buscar y filtrar vacantes activas de todas las areas (CU_10, RF-13, RN-11).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

import { NOMBRE_SENIORIDAD } from "../catalogos.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER", "RRHH"]);
const resultados = document.getElementById("resultados");
const mensaje = document.getElementById("mensaje");
const campos = ["titulo", "area", "senioridad", "habilidadId"].map((id) => document.getElementById(id));
const botonFiltros = document.getElementById("mostrar-filtros");
const avanzados = document.getElementById("filtros-avanzados");

function tarjeta(v) {
  const chips = (lista) => lista.map((h) => `<span class="etiqueta">${escaparHtml(h.nombre)}</span>`).join(" ");
  return `<article class="tarjeta">
    <h2>${escaparHtml(v.titulo)}</h2>
    <p class="ayuda">${escaparHtml(v.area)} · ${NOMBRE_SENIORIDAD[v.senioridad]}
      ${v.proyecto ? ` · ${escaparHtml(v.proyecto.nombre)}` : ""} · publicada el ${formatearFecha(v.fechaActivacion)}</p>
    ${v.descripcion ? `<p>${escaparHtml(v.descripcion)}</p>` : ""}
    <p><strong>Críticas:</strong> ${chips(v.habilidadesCriticas)}</p>
    ${v.habilidadesDeseables.length ? `<p><strong>Deseables:</strong> ${chips(v.habilidadesDeseables)}</p>` : ""}
    <a class="boton boton-primario" href="vacante.html?id=${v.id}">Ver detalle</a>
  </article>`;
}

let temporizador;
async function buscar() {
  const parametros = new URLSearchParams();
  campos.forEach((c) => {
    if (c.value.trim()) {
      parametros.set(c.id, c.value.trim());
    }
  });
  try {
    const lista = await request("GET", `/vacantes?${parametros}`);
    resultados.innerHTML = lista.length === 0
      ? '<p class="vacio">No hay vacantes activas con esos filtros.</p>'
      : lista.map(tarjeta).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

campos.forEach((c) => c.addEventListener("input", () => {
  clearTimeout(temporizador);
  temporizador = setTimeout(buscar, 250);
}));
document.getElementById("form-filtros").addEventListener("submit", (e) => e.preventDefault());
botonFiltros.addEventListener("click", () => {
  avanzados.hidden = !avanzados.hidden;
  botonFiltros.setAttribute("aria-expanded", String(!avanzados.hidden));
  botonFiltros.textContent = avanzados.hidden ? "Más filtros" : "Menos filtros";
});

async function iniciar() {
  try {
    const catalogo = await request("GET", "/habilidades");
    document.getElementById("habilidadId").insertAdjacentHTML("beforeend",
      catalogo.map((h) => `<option value="${h.id}">${escaparHtml(h.nombre)}</option>`).join(""));
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
  await buscar();
}

if (usuario) {
  iniciar();
}
