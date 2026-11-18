# Configuración

TalentMatch separa tres tipos de configuración:

| Tipo | Archivo | Se sube a Git |
|---|---|---|
| **Parámetros de negocio** (umbral, pesos, plazos) | `conf/config.example.yaml` (plantilla) → `conf/config.yaml` (copia local) | La plantilla sí; la copia no |
| **Configuración técnica** de Spring | `backend/src/main/resources/application.yml` | Sí |
| **Secretos y datos del ambiente** | Variables de entorno, o `.env` en local (plantilla `.env.example`) | Nunca |

Orden de carga (`spring.config.import`): `config.example.yaml` → `config.yaml` → `.env`. Si un valor está en varios, **gana el último**. Las variables de entorno del sistema tienen prioridad sobre todo.

## Parámetros de negocio (`conf/config.yaml`, RNF-12)

Ningún valor de negocio está fijo en el código: se leen al arrancar con las clases `config/*Properties`. Para cambiar un valor en producción hay que modificar `conf/config.example.yaml` en el repositorio, porque la imagen Docker lo copia, y volver a desplegar.

| Sección | Clave | Valor | Regla |
|---|---|---|---|
| `algoritmo` | `peso_habilidades` / `peso_senioridad` | 0.7 / 0.3 | RN-23 |
| | `ajuste_senioridad_superior` | 0.9 | RN-23 |
| | `ajuste_senioridad_inferior` | [1.0, 0.6, 0.3, 0.0] | RN-23 |
| | `umbral_viabilidad` | 65 | RN-27, RN-28 |
| `plazos` | `antiguedad_minima_rol_meses` | 6 | RN-07 |
| | `bloqueo_repostulacion_meses` | 6 | RN-10 |
| | `activacion_diferida_horas` | 24 | RN-03 |
| | `ciclo_evaluacion_dias` | 3 | RN-12 |
| | `umbral_vacante_sin_match_dias` | 10 | RN-05, RN-30 |
| `ranking` | `indicador_verde` / `indicador_amarillo` | 80 / 70 | HU-001 (solo visual) |
| `alertas` | `rechazos_viables_para_alerta` | 2 | RN-29 |
| `reporte_rechazos` | `meses_ventana` / `desviacion_resaltado_pct` | 3 / 20 | HU-002 |
| `reporte_ejecutivo` | `top_n` | 10 | RN-21 |
| `costo_rotacion` | salida (6000 + 3000 + 2000 USD) y rotación interna (1000 + 500 + 500 USD) | — | RN-26 (solo informativo, no altera el ranking) |
| `seguridad` | `minutos_inactividad_sesion` | 30 | HU-006 |
| | `longitud_minima_password` | 8 | RNF-07 |
| | `dominios_correo_corporativo` | [talentmatch.com] | RF-01 |
| `rendimiento` | `max_segundos_respuesta` / `max_segundos_ranking` / `max_usuarios_concurrentes` / `milisegundos_indicador_carga` | 3 / 2 / 50 / 300 | RNF-01, RNF-04, HU-005 |

> Si cambias `minutos_inactividad_sesion`, cambia también `MINUTOS_INACTIVIDAD` en `frontend/js/auth.js`.

## `application.yml`: claves importantes

| Clave | Valor o variable | Para qué |
|---|---|---|
| `spring.datasource.url` / `username` / `password` | `${DB_URL}` / `${DB_USER}` / `${DB_PASSWORD}` | Conexión a PostgreSQL |
| `spring.flyway.locations` | `${FLYWAY_LOCATIONS:...migrations,...seeds}` | Qué carpetas aplica Flyway |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate solo valida; no crea tablas |
| `spring.jpa.open-in-view` | `false` | No mantener la sesión de BD abierta mientras se arma el JSON |
| `server.port` | `${PORT:8080}` | Render define `PORT`; en local es 8080 |
| `server.forward-headers-strategy` | `framework` | Respetar `X-Forwarded-Proto` detrás del proxy (HTTPS) |
| `logging.file.name` | `${LOG_FILE:logs/talentmatch.log}` | Log. Los tiempos de respuesta llevan la etiqueta `tiempo-respuesta` |
| `talentmatch.cors.origenes-permitidos` | `${CORS_ORIGENES:http://localhost:5500,...}` | Orígenes del frontend (admite comodines) |
| `talentmatch.tareas.habilitadas` | `${TAREAS_HABILITADAS:true}` | Apaga los cron. Las pruebas los apagan |
| `talentmatch.tareas.*-cron` | Ver [Arquitectura](Technical-Architecture#6-tareas-programadas-cron) | Horarios de las tareas |

## Variables de entorno por ambiente

| Variable | Local (`.env`) | CI (`ci.yml`) | Render (producción) | Vercel |
|---|---|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/talentmatch` | `jdbc:postgresql://localhost:5432/talentmatch_test` | `jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require` | — |
| `DB_USER` | Tu usuario local | `talentmatch` | `postgres.<id-del-proyecto-supabase>` | — |
| `DB_PASSWORD` | Tu clave local | `''` (trust) | Clave del proyecto de Supabase (secreta) | — |
| `JWT_SECRET` | Opcional, 32+ caracteres | No (se genera aleatoria) | Generada por Render | — |
| `CORS_ORIGENES` | No (usa el valor por defecto) | No | `https://talentmatch.vercel.app,https://talentmatch-*.vercel.app` | — |
| `FLYWAY_LOCATIONS` | No (migrations + seeds) | No | `filesystem:/app/database/migrations,filesystem:/app/database/seeds` | — |
| `JAVA_TOOL_OPTIONS` | No | No | `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC` (512 MB de RAM) | — |
| `PORT` | No | No | La define Render | — |
| `TALENTMATCH_API_URL` | — | — | — | `https://talentmatch-n5kp.onrender.com/api/v1` |

Variables que fija la imagen Docker (`backend/Dockerfile`): `TALENTMATCH_CONF_DIR=/app/conf`, `TALENTMATCH_DB_DIR=/app/database` y `FLYWAY_LOCATIONS=filesystem:/app/database/migrations`. Esta última la sobrescribe Render con su propio valor.

## CORS

Para agregar un dominio nuevo del frontend, súmalo a `CORS_ORIGENES` en Render, separado por comas. Se admiten patrones con `*`.
