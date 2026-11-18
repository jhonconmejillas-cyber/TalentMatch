# Seguridad

Requisitos que cubre: RNF-07 (credenciales, HTTPS y sesión), RNF-10 (privacidad del historial), HU-006 (protección de datos sensibles) y RN-18 (anonimato del seleccionado).

## 1. Autenticación

| Aspecto | Implementación |
|---|---|
| Registro | `POST /auth/registro`. Exige un correo de los dominios de `seguridad.dominios_correo_corporativo` (hoy `talentmatch.com`) y una contraseña de al menos `longitud_minima_password` (8) caracteres. Crea un usuario con rol Empleado |
| Contraseñas | **BCrypt** (`PasswordEncoder`). Nunca se guardan ni se devuelven en texto plano |
| Login | `POST /auth/login` devuelve un **JWT firmado con HMAC-SHA256** (`TokenService`, con `NimbusJwtEncoder`/`NimbusJwtDecoder`) |
| Clave del token | Variable de entorno `JWT_SECRET`, de **mínimo 32 caracteres**. Si falta (solo en desarrollo), se genera una aleatoria al arrancar y las sesiones se pierden al reiniciar. En Render la genera la plataforma |
| Contenido del token | `sub` = id del usuario, más el rol y el vencimiento |

## 2. Sesión que expira por inactividad (HU-006)

- El token dura `seguridad.minutos_inactividad_sesion` (**30 min**).
- `RenovacionTokenFilter` emite un **token nuevo en cada petición autenticada**, en el encabezado `X-Token-Renovado`. El frontend lo guarda.
- Mientras el usuario trabaja, la sesión no vence. Tras 30 minutos **sin actividad**, el token vence y la API responde **401**.
- El frontend además cierra la sesión por inactividad (`auth.js`).
- **Cuenta desactivada:** el filtro comprueba en cada petición que el usuario siga activo. Si el Admin lo desactiva, su token deja de servir de inmediato, aunque no haya vencido.

## 3. Autorización: dos niveles

1. **Por rol:** `@PreAuthorize("hasRole('MANAGER')")` en los controladores. Roles: `EMPLEADO`, `MANAGER`, `RRHH`, `LIDER_EJECUTIVO` y `ADMIN`.
2. **Por relación con el recurso**, comprobada en la capa de servicio con `AccesoDenegadoException`. Ejemplos:
   - Solo el **manager actual** del empleado puede avalar sus habilidades (RN-19).
   - Solo el manager **que publicó** la vacante ve su ranking y decide sobre él.
   - Un empleado solo ve el resultado de **sus** postulaciones.
   - Un manager ve el historial de un empleado solo si es su manager actual o si el empleado se postuló a una de sus vacantes, y **nunca** ve sus postulaciones (RNF-10). RR.HH. ve todo.

Cada 403 (por rol o por relación) queda en `bitacora_acceso`, con quién, qué recurso y por qué. RR.HH. la consulta en `GET /gestion/accesos-denegados`. La tabla es de solo lectura gracias a un trigger.

La matriz completa está en `docs/seguridad/Matriz-de-Permisos.md`. **`MatrizPermisosTest` la recorre contra la API real**: si un endpoint deja entrar a un rol que no debía, la prueba falla.

## 4. Privacidad

- **Ranking:** el manager ve a los candidatos con un **alias** (`Empleado_011`), no con su nombre (interpretación del ejemplo de HU-001).
- **Resultado (RN-18):** el empleado rechazado ve el puntaje y las habilidades del seleccionado **sin ningún dato identificable**. `ResultadoControllerTest` revisa el JSON para asegurarlo.
- **Neutralidad (RNF-09):** la fórmula recibe una lista cerrada de datos (habilidades avaladas y senioridad). Ni el nombre, ni la edad, ni la relación con el manager pueden influir.

## 5. HTTPS y encabezados

- Vercel y Render terminan TLS. El backend usa `server.forward-headers-strategy: framework`, respeta `X-Forwarded-Proto` y envía HSTS en las respuestas seguras.
- Vercel agrega `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` y `Referrer-Policy` (`frontend/vercel.json`).

## 6. CORS

`SecurityConfig` usa `setAllowedOriginPatterns`, que acepta orígenes exactos y **comodines**. Los orígenes salen de la variable `CORS_ORIGENES`:
- **Local:** `http://localhost:5500`, `http://127.0.0.1:5500` y `http://localhost:63342`.
- **Producción (Render):** `https://talentmatch.vercel.app,https://talentmatch-*.vercel.app`. El comodín cubre el dominio real `talentmatch-frontend-puce.vercel.app` y los *previews* de cada PR.

Encabezados expuestos al navegador: `X-Token-Renovado` y `Content-Disposition`.

## 7. Secretos

| Secreto | Dónde vive | Nunca en |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `.env` local (ignorado por Git) y variables de entorno de Render | El repositorio |
| `JWT_SECRET` | `.env` local y Render (generado) | El repositorio |
| `SONAR_TOKEN` | GitHub → Settings → Secrets → Actions | El repositorio |

El CI no usa ninguna contraseña: su base de datos temporal acepta conexiones locales sin clave (`POSTGRES_HOST_AUTH_METHOD: trust`) y solo existe durante el job.

## 8. Hallazgos de seguridad corregidos (SonarQube)

| Hallazgo | Corrección |
|---|---|
| DOM XSS en `layout.js` (encabezado con `innerHTML` y datos de la sesión) | Encabezado construido con la API del DOM |
| Contraseña escrita en `ci.yml` | Base del CI sin contraseña |
| Acción de terceros sin fijar (`sonarqube-scan-action@v5`) | Fijada a su SHA completo |
| *Path traversal* en `scripts/reporte_pruebas.py` (rutas por línea de comandos) | Rutas fijas dentro del repositorio |
| Hashes BCrypt de los usuarios de prueba (17 avisos) | **Pendiente, aceptado:** son datos de demostración. En un ambiente real, quitar `seeds` de `FLYWAY_LOCATIONS` |
