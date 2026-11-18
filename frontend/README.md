# Frontend — TalentMatch

Aplicación web en **HTML, CSS y JavaScript sin framework**. Cada pantalla es una página `.html`
con su propio archivo `.js`. Funciona en Chrome y Edge desde 1366×768 (RNF-11).

## Estructura

| Ruta | Contenido |
|---|---|
| `index.html` | Inicio de sesión. |
| `pages/` | Una página por pantalla (registro, paneles por rol, perfil, vacantes, ranking…). |
| `js/api.js` | **Única** puerta hacia el backend: `request(metodo, ruta, datos)` agrega el token y traduce los errores a mensajes en español. |
| `js/auth.js` | Sesión en el navegador y redirección al panel según el rol. |
| `js/config.js` | URL de la API según el ambiente. |
| `js/ui.js` | Utilidades de interfaz (mensajes, errores por campo, escape de HTML). |
| `js/pages/` | Un `.js` por página. |
| `css/estilos.css` | Estilos base. |

## Cómo ejecutarlo

Las páginas usan módulos de JavaScript (`type="module"`), así que deben servirse por HTTP (no
abrirse con doble clic). Con el backend corriendo en `http://localhost:8080`:

```bash
cd frontend
python -m http.server 5500
# abrir http://localhost:5500
```

También sirve la extensión *Live Server* de VS Code (puerto 5500), que ya está autorizado en el
CORS del backend.

## Reglas de interfaz

1. **Máximo 3 clics** hasta cualquier acción principal (RNF-03).
2. **Indicador de carga** para respuestas de más de 300 ms (HU-005, escenario 3).
3. **Opciones avanzadas ocultas** tras un control explícito (RNF-05).
4. **El ranking no se puede arrastrar ni reordenar** (RN-14, CU_14).
5. Todo mensaje de bloqueo debe decir **cuándo** se habilitará la acción (HU-008).
6. Interfaz y mensajes de error **en español** (RNF-13).
7. El frontend **no calcula ni decide** nada del negocio: solo muestra lo que responde la API.
