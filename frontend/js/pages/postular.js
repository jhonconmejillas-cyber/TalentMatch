// Bloque "Postularme" del detalle de vacante (CU_12, HU-008).
// El backend decide si se puede; aqui solo se muestra el boton (o el motivo y la fecha).

import { request } from "../api.js";
import { usuarioActual } from "../auth.js";
import { escaparHtml } from "../ui.js";

const acciones = document.getElementById("acciones");

document.addEventListener("vacante-cargada", async ({ detail: vacante }) => {
  const usuario = usuarioActual();
  if (!usuario || !["EMPLEADO", "MANAGER"].includes(usuario.rol)) {
    return;
  }
  const seccion = document.createElement("section");
  seccion.className = "tarjeta";
  acciones.appendChild(seccion);

  async function pintar() {
    const e = await request("GET", `/vacantes/${vacante.id}/elegibilidad`);
    if (e.yaPostulado) {
      seccion.innerHTML = `<p class="mensaje mensaje-exito">Ya te postulaste a esta vacante.
        Sigue su estado en <a href="mis-postulaciones.html">Mis postulaciones</a>.</p>`;
      return;
    }
    // Escenario 1: boton deshabilitado con el motivo y CUANDO podra postularse.
    seccion.innerHTML = `
      ${e.puedePostularse ? "" : `<p class="mensaje mensaje-alerta">${escaparHtml(e.motivo)}</p>`}
      <p id="resultado-postulacion" class="mensaje" hidden></p>
      <button type="button" class="boton boton-primario" id="postularme" ${e.puedePostularse ? "" : "disabled"}>
        Postularme</button>
      <p class="ayuda">Puedes tener varias postulaciones activas. Si aceptas una vacante, las demás se cancelan solas.</p>`;
    seccion.querySelector("#postularme").addEventListener("click", postular);
  }

  async function postular(evento) {
    const boton = evento.target;
    const resultado = seccion.querySelector("#resultado-postulacion");
    boton.disabled = true;
    try {
      const r = await request("POST", `/vacantes/${vacante.id}/postulaciones`);
      // Escenario 2: postulado, pero con aviso si el puntaje es bajo.
      seccion.innerHTML = `<p class="mensaje mensaje-exito">¡Te postulaste! Tu Puntaje Final es
        ${r.postulacion.puntajeFinal.toFixed(1)} %. Sigue su estado en
        <a href="mis-postulaciones.html">Mis postulaciones</a>.</p>
        ${r.aviso ? `<p class="mensaje mensaje-alerta">${escaparHtml(r.aviso)}</p>` : ""}`;
    } catch (error) {
      resultado.textContent = error.message;
      resultado.className = "mensaje mensaje-error";
      resultado.hidden = false;
      boton.disabled = false;
    }
  }

  try {
    await pintar();
  } catch (error) {
    seccion.innerHTML = `<p class="mensaje mensaje-error">${escaparHtml(error.message)}</p>`;
  }
});
