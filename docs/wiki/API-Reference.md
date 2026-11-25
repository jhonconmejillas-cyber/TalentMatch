# Referencia de la API REST

- **URL base:** `http://localhost:8080/api/v1` en local y `https://talentmatch-n5kp.onrender.com/api/v1` en producción.
- **Formato:** JSON (UTF-8). Los mensajes de error vienen en español en el campo `mensaje` y, si hay errores de validación, en `campos`.
- **Autenticación:** encabezado `Authorization: Bearer <token>` en todo, salvo en los endpoints públicos. Cada respuesta autenticada trae `X-Token-Renovado` con un token nuevo; el frontend lo guarda (ver [Seguridad](Security)).

Roles: **EMP** = Empleado · **MGR** = Manager · **RRHH** = Analista RR.HH. · **LIDER** = Líder Ejecutivo · **ADMIN** = Administrador. 🔗 = además del rol, exige **relación** con el recurso; si no la hay, responde 403 y queda en la bitácora de accesos.

## Públicos

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/health` | `{"estado":"ok"}`. Lo usan Render (health check) y el monitoreo |
| POST | `/auth/registro` | Registro: `nombre`, `correo` del dominio corporativo y `contrasena` de mínimo 8 caracteres. Crea un usuario con rol Empleado |
| POST | `/auth/login` | `{correo, contrasena}` → `{token, expiraEn, usuario}` |

## Sesión y notificaciones (todos los roles)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/me` | Usuario de la sesión |
| GET | `/notificaciones` | Mis notificaciones in-app |
| PATCH | `/notificaciones/{id}` | Marcar como leída (🔗 solo las propias) |
| GET | `/habilidades` | Catálogo de habilidades |
| GET | `/proyectos` | Proyectos activos (para el perfil) |

## Perfil, habilidades y wishlist (EMP, MGR)

| Método | Ruta | Descripción |
|---|---|---|
| GET / PUT | `/empleados/me/perfil` | Ver o editar senioridad, experiencia, área, fecha de inicio del rol y proyecto actual |
| GET / POST | `/empleados/me/habilidades` | Ver o agregar habilidades. Entran como `AGREGADO` (RN-19) |
| GET | `/empleados/me/wishlist` | Habilidades que quiero aprender |
| POST / DELETE | `/empleados/me/wishlist/{habilidadId}` | Agregar o quitar de la wishlist |

## Aval de habilidades (MGR)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/equipo/habilidades` | Habilidades pendientes de aval de mi equipo |
| PATCH | `/habilidades-empleado/{id}` | Avalar o rechazar (🔗 solo el manager **actual** del empleado, RN-19) |

## Vacantes

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/vacantes?titulo=&habilidadId=&area=&senioridad=` | EMP, MGR, RRHH | Buscar y filtrar vacantes activas |
| GET | `/vacantes/{id}` | EMP, MGR, RRHH | Detalle |
| POST | `/vacantes` | MGR | Publicar: título, descripción, senioridad, área, proyecto y habilidades críticas y deseables |
| GET | `/equipo/vacantes` | MGR | Mis vacantes publicadas |
| GET | `/vacantes/{id}/mi-puntaje` | EMP, MGR | Mi Puntaje Final para esa vacante, con desglose y aviso si es menor que 65 (RN-28). Queda en la bitácora |

## Postulación (EMP, MGR)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/vacantes/{id}/elegibilidad` | ¿Puedo postularme? Si no, dice el motivo y desde qué fecha (RN-07, RN-10) |
| POST | `/vacantes/{id}/postulaciones` | Postularse. 409 si incumple una regla. Un puntaje bajo **no** bloquea, solo avisa (RN-28) |
| GET | `/empleados/me/postulaciones` | Mis postulaciones y su estado |

## Ranking y decisión

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/vacantes/{id}/ranking` | MGR 🔗 (quien la publicó), RRHH | Candidatos viables (≥ 65) en orden descendente, con alias `Empleado_011`, indicador (verde ≥ 80, amarillo ≥ 70, rojo) y `habilitadoParaDecidir` según el ciclo de 3 días (RN-12) |
| POST / PUT / PATCH / DELETE | `/vacantes/{id}/ranking/**` | Todos | **Siempre 409**: el ranking es inmutable (RN-14) |
| GET | `/postulaciones/{id}/desglose` | MGR 🔗, RRHH | Desglose del puntaje y costo de rotación (RN-26). Queda en la bitácora |
| POST | `/postulaciones/{id}/aceptar` | MGR 🔗 | Acepta. Cierra la vacante y ejecuta RN-09, RN-15 y RN-16 en una transacción |
| POST | `/postulaciones/{id}/rechazar` | MGR 🔗 | Rechaza sin justificación (RN-17). Cuenta para la alerta de RN-29 |
| GET | `/postulaciones/{id}/resultado` | EMP o MGR 🔗 (la propia), RRHH | Resultado anónimo: puntaje y habilidades del seleccionado, sin datos identificables (RN-18) |

## Gestión de RR.HH.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/alertas?estado=` | Alertas por rechazos repetidos (RN-29) y vacantes sin match (RN-30) |
| PATCH | `/alertas/{id}` | Marcar como atendida |
| GET | `/asignaciones` | Asignaciones vigentes |
| POST | `/asignaciones/{id}/liberar` | Liberar un puesto (renuncia o cierre). Crea la vacante automática (RN-02) |
| GET | `/gestion/vacantes?estado=` | Todas las vacantes por estado |
| GET | `/gestion/accesos-denegados` | Bitácora de accesos denegados (HU-006) |
| GET | `/empleados/{id}/historial` | Historial de proyectos, rotaciones y postulaciones. También MGR 🔗 (manager actual o vacante en común), pero **sin** las postulaciones |
| GET | `/reportes/rechazos?periodo=AAAA-MM` | Reporte de patrones de rechazo por manager (HU-002) |
| GET | `/reportes/rechazos/archivo?periodo=AAAA-MM` | Descarga en CSV |
| GET | `/reportes/rechazos/generados` | Reportes mensuales guardados por la tarea del día 1 |

## Reporte ejecutivo (LIDER, RRHH)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/reportes/top10` | Top 10 por habilidades avaladas, rotaciones y antigüedad (RN-21) |

## Administración (ADMIN)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/usuarios` | Lista de usuarios |
| PATCH | `/usuarios/{id}` | Cambiar el rol o activar/desactivar. Una cuenta desactivada pierde la sesión de inmediato |

## Códigos de respuesta

| Código | Significado en TalentMatch |
|---|---|
| 200 | OK |
| 400 | Datos inválidos (`campos` indica cuáles) |
| 401 | Sin sesión, token vencido (30 min sin actividad) o credenciales incorrectas |
| 403 | Rol incorrecto o sin relación con el recurso (queda en la bitácora) |
| 404 | No existe |
| 409 | Regla de negocio incumplida, o intento de reordenar el ranking |

La matriz de permisos completa, verificada automáticamente por `MatrizPermisosTest`, está en `docs/seguridad/Matriz-de-Permisos.md`.
