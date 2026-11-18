# Guía del frontend

Sitio **estático** en HTML, CSS y JavaScript con **módulos ES nativos**. No hay framework, `npm install` ni compilación: lo que está en `frontend/` es exactamente lo que se publica. Toda la interfaz y los mensajes están en español (RNF-13).

> **Regla fundamental:** el frontend **no calcula ni decide** nada del negocio. Solo muestra lo que responde la API y envía acciones. Toda regla se valida en el backend.

## Estructura

```
frontend/
├── index.html                 Login (página de entrada)
├── pages/*.html               Una página por pantalla
├── css/                       Estilos
├── js/
│   ├── api.js                 Cliente HTTP: request(), descargar(), indicador de carga, renovación del token
│   ├── auth.js                Sesión: guardar/leer token, cierre por inactividad, panel según el rol
│   ├── layout.js              Encabezado común: marca, menú según el rol, usuario y cerrar sesión
│   ├── ui.js                  Utilidades: mensajes, marcar campos con error, escaparHtml, formatearFecha, ejecutar
│   ├── catalogos.js           Catálogos compartidos (senioridades, áreas...)
│   ├── config.js              Calcula la URL de la API según el ambiente
│   ├── ambiente.js            URL de la API del ambiente (la reescribe Vercel al publicar)
│   └── pages/*.js             Un módulo por página
├── scripts/generar-ambiente.mjs   Lo ejecuta Vercel al publicar
└── vercel.json                Configuración de Vercel (build, encabezados de seguridad)
```

## Pantallas por rol

El menú de cada rol está en `MENU_POR_ROL` (`js/layout.js`). Todos los roles tienen además **Notificaciones**.

| Rol | Panel de inicio | Menú |
|---|---|---|
| Empleado | `panel-empleado.html` | Vacantes · Mis postulaciones · Mi perfil · Mis habilidades |
| Manager | `panel-manager.html` | Mis vacantes (publicar) · Avalar habilidades · Mi perfil · Mis habilidades · Vacantes · Mis postulaciones |
| RR.HH. | `panel-rrhh.html` | Alertas · Gestión de vacantes · Reporte de rechazos · Top 10 |
| Líder Ejecutivo | `panel-lider.html` | Reporte top 10 |
| Admin | `panel-admin.html` | Usuarios |

Otras páginas a las que se llega desde las anteriores:
- `vacante.html`: detalle, mi puntaje y botón para postularse.
- `ranking.html`: ranking del manager, con desglose, aceptar y rechazar.
- `resultado.html`: resultado anónimo de una postulación rechazada.
- `registro.html`: alta de cuenta.

## Cómo funciona una página

Cada `pages/X.html` carga primero `../js/ambiente.js` (script clásico) y luego su módulo `../js/pages/X.js`. El módulo sigue este patrón:

```js
import { request } from "../api.js";
import { montarLayout } from "../layout.js";
import { ejecutar, escaparHtml, mostrarMensaje } from "../ui.js";

const usuario = montarLayout(["EMPLEADO", "MANAGER"]); // protege la página y dibuja el encabezado
async function cargar() {
  try {
    const datos = await request("GET", "/empleados/me/postulaciones");
    // pintar con escaparHtml() en todo dato que venga del servidor
  } catch (error) {
    mostrarMensaje(mensaje, error.message);              // mensaje en español que envía la API
  }
}
if (usuario) {
  ejecutar(cargar());                                   // nunca dejar una promesa suelta
}
```

## Módulos clave

- **`api.js`, función `request(metodo, ruta, datos)`**
  - Agrega `Authorization: Bearer <token>`.
  - Muestra un indicador de carga si la respuesta tarda más de 300 ms (RNF-04).
  - Si la respuesta trae `X-Token-Renovado`, guarda el token nuevo.
  - Ante un **401** cierra la sesión y lleva al login.
  - Ante otros errores lanza `ErrorApi`, con el mensaje del servidor y los `campos` con error.
  - `descargar()` baja archivos, como el CSV del reporte de rechazos.
- **`auth.js`**
  - Guarda el token y el usuario en `sessionStorage`, así que la sesión se pierde al cerrar la pestaña.
  - Vigila la actividad (clics y teclas). Tras **30 minutos** sin actividad cierra la sesión y lleva a `index.html?expirada=1`.
  - `requerirSesion(roles)` redirige si no hay sesión o si el rol no corresponde.
  - El valor de 30 minutos debe coincidir con `seguridad.minutos_inactividad_sesion` de `conf/config.yaml`.
- **`layout.js`:** arma el encabezado con la **API del DOM** (`createElement`, `textContent`). **No usa `innerHTML` con datos de la sesión**: ese fue un XSS que reportó SonarQube y se corrigió.
- **`ui.js`**
  - `escaparHtml()` es obligatorio al interpolar datos en HTML.
  - `ejecutar(promesa)` lanza una tarea `async` sin esperarla y registra cualquier error que la tarea no haya controlado. Corrige las "promesas sueltas" que reportó SonarQube.

## URL de la API según el ambiente (`config.js` + `ambiente.js`)

1. Si existe `window.TALENTMATCH_API_URL` (definida en `ambiente.js`), se usa esa.
2. Si la página está en `localhost` o `127.0.0.1`, se usa `http://localhost:8080/api/v1`.
3. Si no, se usa `<mismo dominio>/api/v1`.

En el repositorio `ambiente.js` deja la URL vacía. Al publicar, **Vercel** ejecuta `node scripts/generar-ambiente.mjs` (el `buildCommand` de `vercel.json`), que reescribe `js/ambiente.js` con el valor de la variable `TALENTMATCH_API_URL` configurada en Vercel.

## Correr el frontend en local

Sirve la carpeta `frontend/` con cualquier servidor estático; los módulos ES no funcionan abriendo el archivo con `file://`:
- VS Code → extensión **Live Server** → *Open with Live Server* (puerto 5500).
- O desde `frontend/`: `python -m http.server 5500`.

El backend en local acepta CORS desde `localhost:5500` y `localhost:63342` (IntelliJ). Ver [Configuración](Configuration#cors).

## Cómo agregar una pantalla

1. Crea `pages/mi-pagina.html` copiando una existente: encabezado, `<script src="../js/ambiente.js"></script>` y `<script type="module" src="../js/pages/mi-pagina.js"></script>`.
2. Crea `js/pages/mi-pagina.js` con el patrón de arriba: `montarLayout(roles)`, `request()`, `escaparHtml()` y `ejecutar()`.
3. Si va en el menú, agrégala a `MENU_POR_ROL` en `js/layout.js`.
4. Nunca pongas lógica de negocio. Si necesitas un dato calculado, pídelo a la API.
