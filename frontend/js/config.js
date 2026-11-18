// Configuracion del frontend: URL de la API segun el ambiente.
//
// - En local (localhost / 127.0.0.1) se usa el backend de la maquina: http://localhost:8080
// - En Vercel la URL viene de window.TALENTMATCH_API_URL
//   (js/ambiente.js, que genera scripts/generar-ambiente.mjs al publicar).

const API_LOCAL = "http://localhost:8080/api/v1";

function urlApi() {
  if (window.TALENTMATCH_API_URL) {
    return window.TALENTMATCH_API_URL.replace(/\/$/, "");
  }
  const host = window.location.hostname;
  if (host === "localhost" || host === "127.0.0.1" || host === "") {
    return API_LOCAL;
  }
  // Por defecto, la API se publica en el mismo dominio bajo /api/v1.
  return `${window.location.origin}/api/v1`;
}

export const API_URL = urlApi();
