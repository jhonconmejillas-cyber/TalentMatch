// Utilidades de interfaz compartidas por todas las paginas.

/** Muestra un mensaje en un contenedor (tipo: "error", "exito" o "info"). */
export function mostrarMensaje(contenedor, texto, tipo = "error") {
  contenedor.textContent = texto;
  contenedor.className = `mensaje mensaje-${tipo}`;
  contenedor.hidden = !texto;
}

export function limpiarMensaje(contenedor) {
  mostrarMensaje(contenedor, "", "info");
}

/** Marca un campo con error y muestra el texto debajo (RNF-13: indica el campo y como corregirlo). */
export function marcarCampo(input, texto) {
  input.setAttribute("aria-invalid", "true");
  let ayuda = input.parentElement.querySelector(".error-campo");
  if (!ayuda) {
    ayuda = document.createElement("small");
    ayuda.className = "error-campo";
    input.parentElement.appendChild(ayuda);
  }
  ayuda.textContent = texto;
}

export function limpiarCampos(formulario) {
  formulario.querySelectorAll("[aria-invalid]").forEach((i) => i.removeAttribute("aria-invalid"));
  formulario.querySelectorAll(".error-campo").forEach((e) => e.remove());
}

/** Aplica los errores por campo que devuelve la API ({ campo: mensaje }). */
export function marcarErroresDeApi(formulario, campos) {
  Object.entries(campos || {}).forEach(([nombre, texto]) => {
    const input = formulario.elements.namedItem(nombre);
    if (input) {
      marcarCampo(input, texto);
    }
  });
}

/** Escapa texto para insertarlo en HTML sin riesgo de inyeccion. */
export function escaparHtml(texto) {
  return String(texto ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

export function formatearFecha(iso) {
  if (!iso) {
    return "";
  }
  return new Date(iso).toLocaleDateString("es-CO", { year: "numeric", month: "long", day: "numeric" });
}
