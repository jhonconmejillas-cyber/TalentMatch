// Se ejecuta en Vercel antes de publicar (ver vercel.json).
// Escribe en js/ambiente.js la URL de la API del ambiente, tomada de la variable de entorno
// TALENTMATCH_API_URL que se configura en Vercel (Production y Preview).
import { writeFileSync } from "node:fs";

const url = (process.env.TALENTMATCH_API_URL || "").trim().replace(/\/$/, "");
if (!url) {
  console.warn("TALENTMATCH_API_URL no esta definida: el frontend usara <su dominio>/api/v1.");
}
const contenido =
  "// Generado al publicar en Vercel (scripts/generar-ambiente.mjs). No editar a mano.\n" +
  `window.TALENTMATCH_API_URL = ${JSON.stringify(url)};\n`;
writeFileSync(new URL("../js/ambiente.js", import.meta.url), contenido);
console.log(`js/ambiente.js -> ${url || "(vacia)"}`);
