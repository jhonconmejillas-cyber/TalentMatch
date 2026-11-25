// Ranking inmutable de candidatos y decision del manager (CU_14 ... CU_16, HU-001).
// La tabla no permite arrastrar ni reordenar (RN-14): el orden viene del servidor.

import { request } from "../api.js";
import { TEXTO_ESTADO_POSTULACION } from "../catalogos.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, formatearFecha, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["MANAGER", "RRHH"]);
const id = new URLSearchParams(window.location.search).get("id");
const tabla = document.getElementById("candidatos");
const panel = document.getElementById("desglose");
const seccionBajoUmbral = document.getElementById("seccion-bajo-umbral");
const tablaBajoUmbral = document.getElementById("bajo-umbral");
const mensaje = document.getElementById("mensaje");
const COLOR = { VERDE: "🟢", AMARILLO: "🟡", ROJO: "🔴" };
const esManager = usuario && usuario.rol === "MANAGER";

const chips = (l) => l.map((h) => `<span class="etiqueta">${escaparHtml(h)}</span>`).join(" ") || "—";

function fila(c, vacanteActiva) {
  const acciones = esManager && vacanteActiva
    ? (c.habilitadoParaDecidir
      ? `<button type="button" class="boton boton-exito" data-accion="aceptar" data-id="${c.postulacionId}">Aceptar</button>
         <button type="button" class="boton boton-peligro" data-accion="rechazar" data-id="${c.postulacionId}">Rechazar</button>`
      : '<span class="ayuda">Próximo ciclo</span>')
    : (c.estado !== "ACTIVA"
      ? `<span class="etiqueta etiqueta-${c.estado.toLowerCase()}">${TEXTO_ESTADO_POSTULACION[c.estado]}</span>` : "");
  return `<tr data-id="${c.postulacionId}" draggable="false" tabindex="0">
    <td>${c.posicion}</td>
    <td>${COLOR[c.indicador]} ${escaparHtml(c.candidato)}</td>
    <td><strong>${c.puntajeFinal.toFixed(1)} %</strong></td>
    <td>${formatearFecha(c.fechaPostulacion)}</td>
    <td>${acciones}</td>
  </tr>`;
}

async function cargar() {
  const r = await request("GET", `/vacantes/${id}/ranking`);
  document.getElementById("titulo").textContent = `Ranking · ${r.vacante.titulo}`;
  document.getElementById("ciclo").textContent = r.corteCicloActual
    ? `Ciclo de evaluación cada ${r.cicloEvaluacionDias} días. Puedes resolver las peticiones recibidas antes del `
      + `${new Date(r.corteCicloActual).toLocaleString("es-CO")}; el próximo lote se habilita el `
      + `${new Date(r.proximoCorte).toLocaleString("es-CO")}.`
    : `El primer lote de peticiones se habilita el ${r.proximoCorte ? new Date(r.proximoCorte).toLocaleString("es-CO") : "—"}.`;
  const activa = r.vacante.estado === "ACTIVA";
  tabla.innerHTML = r.candidatos.length === 0
    ? `<tr><td colspan="5" class="vacio">${activa ? "Aún no hay candidatos viables."
      : "La vacante se cerró sin candidatos viables."}</td></tr>`
    : r.candidatos.map((c) => fila(c, activa)).join("");
  pintarBajoUmbral(r.postuladosBajoUmbral || [], r.umbralViabilidad);
}

// RN-28: quienes se postularon con puntaje bajo no entran al ranking ni se pueden aceptar,
// pero el manager debe saber que existen (antes quedaban ocultos y parecia que nadie se postulo).
function pintarBajoUmbral(lista, umbral) {
  seccionBajoUmbral.hidden = lista.length === 0;
  document.getElementById("titulo-bajo-umbral").textContent = `Postulados por debajo del umbral (${lista.length})`;
  document.getElementById("ayuda-bajo-umbral").textContent = `Se postularon, pero su Puntaje Final es menor `
    + `al ${umbral.toFixed(0)} %: no entran al ranking ni se pueden aceptar (RN-28). Selecciona uno para ver su desglose.`;
  tablaBajoUmbral.innerHTML = lista.map((c) => `<tr data-id="${c.postulacionId}" draggable="false" tabindex="0">
    <td>${COLOR.ROJO} ${escaparHtml(c.candidato)}</td>
    <td>${c.puntajeFinal.toFixed(1)} %</td>
    <td>${formatearFecha(c.fechaPostulacion)}</td>
    <td>${c.estado === "ACTIVA" ? "" : `<span class="etiqueta etiqueta-${c.estado.toLowerCase()}">`
      + `${TEXTO_ESTADO_POSTULACION[c.estado]}</span>`}</td>
  </tr>`).join("");
}

// Escenario 2: el desglose se abre al lado, sin perder el ranking.
async function verDesglose(idPostulacion) {
  [tabla, tablaBajoUmbral].forEach((t) => t.querySelectorAll("tr")
    .forEach((tr) => tr.classList.toggle("resaltada", tr.dataset.id === idPostulacion)));
  panel.innerHTML = '<p class="vacio">Cargando…</p>';
  try {
    const d = await request("GET", `/postulaciones/${idPostulacion}/desglose`);
    const x = d.desglose;
    panel.innerHTML = `
      <h2>${escaparHtml(d.candidato)} · ${d.puntajeFinal.toFixed(1)} %</h2>
      <h3>Match Score</h3>
      <p>Habilidades avaladas: ${(x.porcentajeHabilidades * 100).toFixed(0)} % × ${x.pesoHabilidades}<br>
         Ajuste por senioridad: ${x.ajusteSenioridad} × ${x.pesoSenioridad}</p>
      <p><strong>Coinciden:</strong> ${chips(x.habilidadesCoincidentes)}</p>
      <p><strong>Le faltan:</strong> ${chips(x.habilidadesFaltantes)}</p>
      ${d.costoRotacion ? `<h3>Costo de Rotación (informativo)</h3>
      <table class="tabla">
        <tr><td>Si renuncia (reemplazo)</td><td>USD ${d.costoRotacion.costoSiRenuncia.toLocaleString("es-CO")}</td></tr>
        <tr><td>Si rota internamente</td><td>USD ${d.costoRotacion.costoSiRota.toLocaleString("es-CO")}</td></tr>
        <tr><td><strong>Ahorro</strong></td><td><strong>USD ${d.costoRotacion.ahorro.toLocaleString("es-CO")}</strong></td></tr>
      </table>
      <p class="ayuda">${escaparHtml(d.costoRotacion.mensaje)} No altera el ranking.</p>` : ""}`;
  } catch (error) {
    panel.innerHTML = `<p class="mensaje mensaje-error">${escaparHtml(error.message)}</p>`;
  }
}

async function decidir(boton) {
  // Confirmacion en dos pasos, sin dialogos del navegador.
  if (!boton.dataset.confirmar) {
    boton.dataset.confirmar = "1";
    boton.textContent = boton.dataset.accion === "aceptar" ? "Confirmar aceptación" : "Confirmar rechazo";
    return;
  }
  boton.disabled = true;
  try {
    const r = await request("POST", `/postulaciones/${boton.dataset.id}/${boton.dataset.accion}`);
    mostrarMensaje(mensaje, r.mensaje, "exito");
    await cargar();
  } catch (error) {
    mostrarMensaje(mensaje, error.message);
    boton.disabled = false;
  }
}

tabla.addEventListener("click", (e) => {
  const boton = e.target.closest("button[data-accion]");
  if (boton) {
    ejecutar(decidir(boton));
    return;
  }
  const tr = e.target.closest("tr[data-id]");
  if (tr) {
    ejecutar(verDesglose(tr.dataset.id));
  }
});
tablaBajoUmbral.addEventListener("click", (e) => {
  const tr = e.target.closest("tr[data-id]");
  if (tr) {
    ejecutar(verDesglose(tr.dataset.id));
  }
});
// RN-14: no se permite arrastrar filas
tabla.addEventListener("dragstart", (e) => e.preventDefault());
tablaBajoUmbral.addEventListener("dragstart", (e) => e.preventDefault());

if (usuario && id) {
  cargar().catch((e) => mostrarMensaje(mensaje, e.message));
}
