# TalentMatch — Matriz de permisos por rol

| Campo | Valor |
|---|---|
| **Documento** | Matriz de permisos (HU-006, RNF-07, RNF-10) |
| **Verificación** | `MatrizPermisosTest` recorre esta tabla contra la API real; si un endpoint no la respeta, la prueba falla. |

Leyenda: ✅ permitido · ❌ denegado (403) · 🔗 permitido solo con **relación** con el recurso (si no la hay → 403 y registro en `bitacora_acceso`).

## API (`/api/v1`)

| Endpoint | Empleado | Manager | RR.HH. | Líder Ejecutivo | Admin |
|---|:-:|:-:|:-:|:-:|:-:|
| `POST /auth/registro`, `POST /auth/login`, `GET /health` | público | público | público | público | público |
| `GET /me`, `GET /notificaciones`, `PATCH /notificaciones/{id}` (solo las propias 🔗) | ✅ | ✅ | ✅ | ✅ | ✅ |
| `GET /usuarios`, `PATCH /usuarios/{id}` | ❌ | ❌ | ❌ | ❌ | ✅ |
| `GET` / `PUT /empleados/me/perfil` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `GET` / `POST /empleados/me/habilidades`, `/empleados/me/wishlist` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `GET /equipo/habilidades` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `PATCH /habilidades-empleado/{id}` (aval) | ❌ | 🔗 manager actual | ❌ | ❌ | ❌ |
| `GET /vacantes`, `GET /vacantes/{id}` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `POST /vacantes`, `GET /equipo/vacantes` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `GET /vacantes/{id}/mi-puntaje`, `/elegibilidad`, `POST /vacantes/{id}/postulaciones` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `GET /empleados/me/postulaciones` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `GET /vacantes/{id}/ranking`, `GET /postulaciones/{id}/desglose` | ❌ | 🔗 quien publicó | ✅ | ❌ | ❌ |
| Reordenar el ranking (`POST`/`PUT`/`PATCH`/`DELETE /vacantes/{id}/ranking`) | ❌ | ❌ (409, RN-14) | ❌ | ❌ | ❌ |
| `POST /postulaciones/{id}/aceptar` / `rechazar` | ❌ | 🔗 quien publicó | ❌ | ❌ | ❌ |
| `GET /postulaciones/{id}/resultado` (RN-18) | 🔗 la propia | 🔗 la propia | ✅ | ❌ | ❌ |
| `GET /empleados/{id}/historial` (proyectos y rotaciones) | ❌ | 🔗 manager actual o vacante en común (sin postulaciones) | ✅ (con postulaciones) | ❌ | ❌ |
| `GET /alertas`, `PATCH /alertas/{id}` | ❌ | ❌ | ✅ | ❌ | ❌ |
| `GET /asignaciones`, `POST /asignaciones/{id}/liberar`, `GET /gestion/**` | ❌ | ❌ | ✅ | ❌ | ❌ |
| `GET /reportes/rechazos/**` | ❌ | ❌ | ✅ | ❌ | ❌ |
| `GET /reportes/top10` | ❌ | ❌ | ✅ | ✅ | ❌ |

## Otras garantías

| Garantía | Cómo se cumple |
|---|---|
| Contraseñas con hash (RNF-07) | BCrypt en el registro (`AuthService`); nunca se devuelven en ninguna respuesta. |
| HTTPS (RNF-07) | Render y Vercel terminan TLS; el backend respeta `X-Forwarded-Proto` y envía HSTS en las respuestas seguras. |
| Sesión que expira (HU-006, escenario 3) | Token de 30 min (`seguridad.minutos_inactividad_sesion`) renovado en cada petición (`X-Token-Renovado`): tras 30 min **sin actividad** el token vence y la API responde 401. El frontend además cierra la sesión por inactividad. |
| Cuenta desactivada | Su token deja de servir de inmediato, aunque no haya vencido. |
| Accesos denegados | Cada 403 queda en `bitacora_acceso` (quién, qué recurso, por qué), consultable por RR.HH. en `GET /gestion/accesos-denegados`. |
| Anonimato del seleccionado (RN-18) | El DTO del resultado no tiene campos identificables; `ResultadoControllerTest` revisa el JSON. |
