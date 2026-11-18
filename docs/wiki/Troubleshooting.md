# Solución de problemas

Errores reales que aparecieron en el proyecto y cómo se resolvieron.

## Backend y base de datos

| Síntoma | Causa | Solución |
|---|---|---|
| `java.net.UnknownHostException: ...pooler.supabase.com` al arrancar en Render | `DB_URL` tenía el texto de ejemplo con `...`, no el host real | En `DB_URL` va `jdbc:postgresql://<host-real>.pooler.supabase.com:5432/postgres?sslmode=require`. El host sale de Supabase → Connect → JDBC → Session pooler (ver [Despliegue](Deployment#base-de-datos-supabase)) |
| `The connection attempt failed` con la cadena de Supabase | Se usó la conexión directa (solo IPv6) | Usar el **Session pooler** (IPv4) |
| La URL de Supabase empieza con `postgresql://usuario:clave@host...` | Formato URI, no JDBC | Separarla: `DB_URL` con `jdbc:` y sin usuario ni clave; `DB_USER` = `postgres.<id>`; `DB_PASSWORD` aparte y sin corchetes |
| `Validate failed: Migration checksum mismatch for migration version 1.1` | Se editó una migración ya aplicada, aunque sea un comentario | Revertir el archivo y hacer el cambio en una migración **nueva** (ver [Modelo de datos](Database-Model#reglas-de-oro-de-flyway)) |
| `Detected resolved migration not applied to database` / fuera de orden | Se agregó una versión menor que la última aplicada | Usar una versión mayor (`V6__...`) |
| `Schema-validation: missing column` al arrancar | La entidad JPA no coincide con la tabla (`ddl-auto: validate`) | Crear la migración que agrega la columna o corregir la entidad |
| `JWT_SECRET debe tener al menos 32 caracteres` | Secreto demasiado corto | Usar 32 caracteres o más, o dejarlo vacío en local |
| `Could not resolve placeholder 'DB_URL'` | Falta la variable de entorno o el `.env` | `cp .env.example .env` y completarlo |
| En las pruebas, un INSERT choca con `ux_asignacion_vigente` | Hibernate ejecuta los INSERT antes que los UPDATE | `saveAndFlush` al liberar la asignación antes de crear la nueva |
| Una prueba lee datos viejos con `JdbcTemplate` | JPA no ha hecho *flush* dentro de la transacción | `em.flush()` (y `em.clear()`) antes de consultar |

## Login y sesión

| Síntoma | Causa | Solución |
|---|---|---|
| "Credenciales incorrectas" con los usuarios de prueba | La base se creó sin los seeds (`FLYWAY_LOCATIONS` sin `seeds`) o se usa la contraseña vieja | `FLYWAY_LOCATIONS` debe incluir `filesystem:/app/database/seeds`. La contraseña es `talento` (antes `TalentMatch2026`) |
| La sesión se cierra sola | 30 minutos sin actividad (diseño, HU-006) | Normal |
| No puedo registrarme con `talento` | El registro exige un mínimo de 8 caracteres | Usar una contraseña más larga; `talento` solo existe para los seeds |
| 403 inesperado | Rol incorrecto o sin relación con el recurso | Revisar `docs/seguridad/Matriz-de-Permisos.md`. RR.HH. ve el motivo en "accesos denegados" |

## Frontend y despliegue

| Síntoma | Causa | Solución |
|---|---|---|
| Error de CORS en la consola del navegador | El dominio del frontend no está en `CORS_ORIGENES` | Agregarlo en Render (se aceptan patrones como `https://talentmatch-*.vercel.app`) |
| El frontend en Vercel llama a la URL equivocada | `TALENTMATCH_API_URL` mal configurada | Corregirla en Vercel y hacer *Redeploy*. Comprobar con `/js/ambiente.js` |
| La primera carga tarda unos 50 s | El plan Free de Render duerme el servicio | Normal. Para evitarlo hace falta un plan de pago o un *ping* periódico |
| Los módulos JS no cargan en local | Se abrió el HTML con `file://` | Servir la carpeta con Live Server o `python -m http.server 5500` |
| Las vacantes "pendientes" no se activan | Render estaba dormido (los cron no corren) | Se activan en el siguiente minuto con el servicio despierto |

## CI y calidad

| Síntoma | Causa | Solución |
|---|---|---|
| `wget: Failed to fetch apache-maven-...zip` | Falla de red del runner | *Re-run failed jobs* |
| El paso de SonarQube aparece *skipped* | Falta el secreto `SONAR_TOKEN` | Crearlo en GitHub → Secrets → Actions |
| Sonar: "running CI analysis while Automatic Analysis is enabled" | El análisis automático se reactivó | Desactivarlo en SonarQube → Administration → Analysis Method |
| Sonar: "sonar.sources, sonar.tests have no effect" | El scanner nuevo analiza todo el repositorio | Ya se quitaron. Usar `sonar.exclusions` o `sonar.coverage.exclusions` |
| Sonar: "Data Dictionary is not configured for the PLSQL analyzer" | Trata los `.sql` como PL/SQL de Oracle | Informativo; ignorar |
| La cobertura en Sonar aparece en ~43 % | Cuenta las pruebas y el frontend como código por cubrir | `sonar.coverage.exclusions` (ya configurado) |
| CodeRabbit no comenta un PR | El PR va hacia una rama que no está en `base_branches`, o se agotó el cupo (10 por hora) | Comentar `@coderabbitai review` o esperar |
| Un PR no se puede fusionar ("required check") | "Compilar y probar backend" falló | Revisar el informe en el *Summary* de la ejecución y el artefacto `informe-pruebas` |
