// Reporte ejecutivo top 10 (CU_22, HU-010). Solo Lider Ejecutivo y RR.HH.

import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { escaparHtml, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["LIDER_EJECUTIVO", "RRHH"]);

async function cargar() {
  try {
    const r = await request("GET", "/reportes/top10");
    document.getElementById("aviso").textContent = r.aviso; // escenario 3: solo un insumo
    document.getElementById("filas").innerHTML = r.empleados.map((e) => `<tr>
        <td>${e.posicion}</td><td>${escaparHtml(e.nombre)}</td><td>${escaparHtml(e.area || "—")}</td>
        <td><strong>${e.habilidadesAvaladas}</strong></td><td>${e.rotaciones}</td><td>${e.aniosEnLaEmpresa}</td>
      </tr>`).join("");
  } catch (error) {
    mostrarMensaje(document.getElementById("mensaje"), error.message);
  }
}

if (usuario) {
  cargar();
}
