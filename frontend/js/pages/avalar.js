// Bandeja del manager para avalar o rechazar habilidades de su equipo (CU_06, RF-07).

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const TEXTO_ESTADO = { AGREGADO: "Pendiente de aval", AVALADO: "Avalada", RECHAZADO: "No avalada" };

const usuario = montarLayout(["MANAGER"]);
const filas = document.getElementById("filas");
const mensaje = document.getElementById("mensaje");
const verTodas = document.getElementById("ver-todas");

async function cargar() {
  try {
    const ruta = verTodas.checked ? "/equipo/habilidades" : "/equipo/habilidades?estado=AGREGADO";
    const lista = await request("GET", ruta);
    filas.innerHTML = lista.length === 0
      ? '<tr><td colspan="6" class="vacio">No hay habilidades pendientes de aval en tu equipo.</td></tr>'
      : lista.map(fila).join("");
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

function fila(h) {
  const acciones = h.estado === "AGREGADO"
    ? `<button type="button" class="boton boton-exito" data-id="${h.id}" data-estado="AVALADO">Avalar</button>
       <button type="button" class="boton boton-peligro" data-id="${h.id}" data-estado="RECHAZADO">No avalar</button>`
    : "";
  return `<tr>
    <td>${escaparHtml(h.empleado)}</td>
    <td>${escaparHtml(h.habilidad)}</td>
    <td>${escaparHtml(h.nivelDeclarado || "—")}</td>
    <td>${formatearFecha(h.fechaAgregado)}</td>
    <td><span class="etiqueta etiqueta-${h.estado.toLowerCase()}">${TEXTO_ESTADO[h.estado]}</span></td>
    <td>${acciones}</td>
  </tr>`;
}

filas.addEventListener("click", async (e) => {
  const boton = e.target.closest("button[data-id]");
  if (!boton) {
    return;
  }
  boton.disabled = true;
  try {
    const h = await request("PATCH", `/habilidades-empleado/${boton.dataset.id}`, { estado: boton.dataset.estado });
    mostrarMensaje(mensaje, `${h.habilidad} de ${h.empleado}: ${TEXTO_ESTADO[h.estado].toLowerCase()}.`, "exito");
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    boton.disabled = false;
  }
});

verTodas.addEventListener("change", cargar);

if (usuario) {
  cargar();
}
