# Despliegue: Vercel + Render + Supabase

```
Usuario ──HTTPS──► Vercel (frontend estático) ──HTTPS /api/v1──► Render (1 contenedor: backend) ──JDBC/TLS──► Supabase (PostgreSQL)
```

| Componente | Servicio y plan | URL | Se despliega |
|---|---|---|---|
| Frontend | Vercel, Hobby (gratis) | https://talentmatch-frontend-puce.vercel.app | Automático en cada push. `main` va a producción y las demás ramas a *previews* |
| Backend | Render, Web Service Docker, Free | https://talentmatch-n5kp.onrender.com (health: `/api/v1/health`) | Automático en cada push a **`main`** |
| Base de datos | Supabase, Free | Session pooler `aws-1-us-west-2.pooler.supabase.com:5432` | Flyway aplica las migraciones al arrancar el backend |

> **Por qué no Cloudflare:** el diseño original usaba Cloudflare Pages y Cloudflare Containers, pero Containers exige el plan de pago *Workers Paid*. Se migró a Vercel + Render en el PR #66.
>
> **Por qué no todo en Vercel:** Vercel no ejecuta Java/Spring Boot; solo sitios estáticos y funciones en JS o Python.
>
> **Por qué un contenedor:** Render no tiene Java nativo, así que el backend (un monolito) se empaqueta en **una** imagen Docker. No son microservicios.

## Base de datos (Supabase)

1. Proyecto creado en supabase.com. La contraseña de la base la guarda el equipo; **no está en el repositorio**.
2. **Connect → Connection String → JDBC → Method: Session pooler**. Render solo tiene IPv4, y la conexión directa de Supabase es IPv6, por eso se usa el pooler.
3. La cadena que muestra Supabase se separa en tres variables:

```
Supabase muestra:  postgresql://postgres.pwnhswwppagaxlatprvr:[YOUR-PASSWORD]@aws-1-us-west-2.pooler.supabase.com:5432/postgres
                                └──────── DB_USER ────────┘ └─ DB_PASSWORD ─┘ └────────────── host ──────────────┘

DB_URL      = jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require
DB_USER     = postgres.pwnhswwppagaxlatprvr
DB_PASSWORD = (la clave del proyecto, sola, sin corchetes)
```

La URL debe empezar con `jdbc:` y **no** lleva el usuario ni la clave. No hace falta crear tablas: Flyway lo hace al arrancar.

## Backend (Render)

El servicio se creó como **Web Service** con *Git Provider* (GitHub, repositorio TalentMatch):

| Campo | Valor |
|---|---|
| Name | `TalentMatch` (URL asignada: `talentmatch-n5kp.onrender.com`) |
| Language | Docker |
| Branch | `main` |
| Region | Virginia (US East) |
| Root Directory | vacío (raíz del repositorio) |
| Dockerfile Path | `./backend/Dockerfile` |
| Docker Build Context | `.` |
| Health Check Path | `/api/v1/health` |
| Instance Type | Free |

Las variables de entorno están en [Configuración](Configuration#variables-de-entorno-por-ambiente). El archivo `render.yaml` del repositorio describe el mismo servicio como *Blueprint*, por si hay que recrearlo con **New → Blueprint**.

**La imagen (`backend/Dockerfile`)** es multi-etapa:
1. `eclipse-temurin:21-jdk`: descarga las dependencias (capa en caché) y ejecuta `./mvnw package -DskipTests`. Las pruebas ya corrieron en el CI.
2. `eclipse-temurin:21-jre`: copia el `.jar`, `conf/config.example.yaml`, `database/migrations` y `database/seeds`, y ejecuta con un usuario sin privilegios (`talentmatch`, uid 1001).

**Limitaciones del plan Free:**
- Se duerme tras **15 minutos** sin tráfico; la primera petición después tarda unos **50 segundos**.
- Tiene 512 MB de RAM.
- Mientras duerme no corren las tareas programadas.

**Volver a desplegar:** cualquier push a `main` despliega solo. A mano: Render → servicio → **Manual Deploy → Deploy latest commit**. Los logs están en Render → servicio → **Logs**.

## Frontend (Vercel)

| Campo | Valor |
|---|---|
| Proyecto | `talentmatch-frontend` |
| Root Directory | `frontend` |
| Framework Preset | Other |
| Build Command | `node scripts/generar-ambiente.mjs` (definido en `vercel.json`) |
| Output Directory | `.` |
| Variable de entorno | `TALENTMATCH_API_URL = https://talentmatch-n5kp.onrender.com/api/v1` (Production y Preview) |

Al publicar, el script escribe la URL en `js/ambiente.js`. Si cambia la URL del backend, actualiza esa variable en Vercel y vuelve a desplegar (*Redeploy*).

## Usuarios de prueba

Producción carga los datos de prueba porque `FLYWAY_LOCATIONS` incluye `seeds`. Contraseña de todos: **`talento`**.

| Rol | Correo |
|---|---|
| Admin | `admin@talentmatch.com` |
| RR.HH. | `rrhh@talentmatch.com` |
| Líder Ejecutivo | `lider@talentmatch.com` |
| Managers | `carlos.perez@`, `diana.torres@`, `andres.gomez@talentmatch.com` |
| Empleados | `juan.ramirez@`, `sofia.castro@`, `mateo.vargas@`, `valentina.ruiz@`, `santiago.mora@`, `isabella.herrera@`, `nicolas.rios@`, `camila.ortiz@`, `samuel.medina@`, `mariana.diaz@talentmatch.com` |

La contraseña original era `TalentMatch2026`. Se cambió a `talento` con la migración `V5_1`. Nota: `talento` tiene 7 caracteres, menos que el mínimo de 8 que exige el **registro**; sirve para entrar con estos usuarios, pero no para crear cuentas nuevas.

**Para un ambiente real:** quita `,filesystem:/app/database/seeds` de `FLYWAY_LOCATIONS` **antes** del primer arranque contra una base nueva.

## Checklist después de desplegar

1. `curl https://talentmatch-n5kp.onrender.com/api/v1/health` responde `{"estado":"ok"}`.
2. CORS: una petición `OPTIONS` con `Origin: https://talentmatch-frontend-puce.vercel.app` responde `access-control-allow-origin` con ese origen.
3. `https://talentmatch-frontend-puce.vercel.app/js/ambiente.js` contiene la URL de Render.
4. Iniciar sesión con un usuario de prueba.

Si algo falla, ve a [Solución de problemas](Troubleshooting).
