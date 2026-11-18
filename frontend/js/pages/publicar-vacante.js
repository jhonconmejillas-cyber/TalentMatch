// Publicar una vacante interna (CU_09, RF-11) y ver las vacantes propias.

import { request } from "../api.js";
import { TEXTO_ESTADO_VACANTE } from "../catalogos.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, formatearFecha, limpiarCampos, marcarErroresDeApi, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["MANAGER"]);
const formulario = document.getElementById("form-vacante");
const mensaje = document.getElementById("mensaje");
const misVacantes = document.getElementById("mis-vacantes");

const seleccionados = (select) => [...select.selectedOptions].map((o) => Number(o.value));

async function cargarMisVacantes() {
  const lista = await request("GET", "/equipo/vacantes");
  misVacantes.innerHTML = lista.length === 0
    ? '<tr><td colspan="5" class="vacio">Aún no has publicado vacantes.</td></tr>'
    : lista.map((v) => `<tr>
        <td>${escaparHtml(v.titulo)}</td>
        <td>${escaparHtml(v.area)}</td>
        <td><span class="etiqueta etiqueta-${v.estado.toLowerCase()}">${TEXTO_ESTADO_VACANTE[v.estado]}</span></td>
        <td>${formatearFecha(v.fechaActivacion)}</td>
        <td><a href="ranking.html?id=${v.id}">Ver ranking</a></td>
      </tr>`).join("");
}

async function iniciar() {
  try {
    const [habilidades, proyectos] = await Promise.all([request("GET", "/habilidades"), request("GET", "/proyectos")]);
    const opciones = habilidades.map((h) => `<option value="${h.id}">${escaparHtml(h.nombre)}</option>`).join("");
    formulario.habilidadesCriticasIds.innerHTML = opciones;
    formulario.habilidadesDeseablesIds.innerHTML = opciones;
    formulario.proyectoId.insertAdjacentHTML("beforeend",
      proyectos.map((p) => `<option value="${p.id}">${escaparHtml(p.nombre)} (${escaparHtml(p.area)})</option>`).join(""));
    await cargarMisVacantes();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
  }
}

formulario.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  limpiarCampos(formulario);
  const datos = {
    titulo: formulario.titulo.value.trim(),
    area: formulario.area.value.trim(),
    senioridad: formulario.senioridad.value || null,
    proyectoId: formulario.proyectoId.value ? Number(formulario.proyectoId.value) : null,
    descripcion: formulario.descripcion.value.trim() || null,
    habilidadesCriticasIds: seleccionados(formulario.habilidadesCriticasIds),
    habilidadesDeseablesIds: seleccionados(formulario.habilidadesDeseablesIds),
  };
  try {
    const v = await request("POST", "/vacantes", datos);
    mostrarMensaje(mensaje, `La vacante "${v.titulo}" quedó publicada y visible para todos los empleados.`, "exito");
    formulario.reset();
    await cargarMisVacantes();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    marcarErroresDeApi(formulario, error.detalles);
  }
});

if (usuario) {
  ejecutar(iniciar());
}
