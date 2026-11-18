# Despliegue de TalentMatch: Vercel + Render + Supabase

```
Usuario (Chrome / Edge)
        |
        v
[ Vercel ]    frontend estatico (carpeta frontend/)          plan Hobby, gratis
        |  HTTPS /api/v1
        v
[ Render ]    backend Spring Boot en un contenedor Docker    plan Free
        |  JDBC con TLS
        v
[ Supabase ]  PostgreSQL                                    plan Free
```

El backend sigue siendo **un solo servicio** (monolito). El contenedor es solo la forma de
empaquetarlo, porque Render no tiene Java nativo: usa `backend/Dockerfile`.

| Rama | Frontend (Vercel) | Backend (Render) |
|---|---|---|
| `main` | Produccion: `https://talentmatch.vercel.app` | Se despliega en cada push a `main` |
| `develop` y PRs | Preview: `https://talentmatch-git-develop-<usuario>.vercel.app` | Usa el mismo backend de produccion |

## 1. Base de datos (Supabase)

1. En [supabase.com](https://supabase.com) crea un proyecto y anota la contraseña de la base.
2. Ve a **Connect → Connection string → JDBC**, elige **Session pooler** (funciona con IPv4,
   que es lo que usa Render). Te da algo como:
   `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?user=postgres.abcd...`
3. Separa los datos:
   - `DB_URL` = `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
   - `DB_USER` = `postgres.abcd...`
   - `DB_PASSWORD` = la contraseña del proyecto

No hace falta crear tablas: **Flyway las crea** al arrancar el backend.

## 2. Backend (Render)

1. En [render.com](https://render.com) entra con GitHub.
2. **New → Blueprint** → elige el repositorio `TalentMatch`. Render lee `render.yaml` y propone
   el servicio `talentmatch-backend` (rama `main`, plan Free).
3. Te pide los valores marcados como secretos: `DB_URL`, `DB_USER`, `DB_PASSWORD` (paso 1).
   `JWT_SECRET` lo genera Render solo.
4. **Apply**. La primera compilación tarda unos 5–8 minutos.
5. Comprueba que responde: `https://talentmatch-backend.onrender.com/api/v1/health`
   (si Render te asignó otro nombre, usa la URL que muestra el panel).

**Datos de prueba:** por defecto en produccion no se cargan los usuarios de prueba. Para una
demo, cambia `FLYWAY_LOCATIONS` en Render a
`filesystem:/app/database/migrations,filesystem:/app/database/seeds` y vuelve a desplegar.

**Limitaciones del plan Free:** el servicio se duerme tras 15 minutos sin peticiones y la
primera peticion despues tarda ~50 segundos. Mientras duerme no corren las tareas programadas
(activacion de vacantes RN-03, alertas RN-05); se ponen al dia en la siguiente ejecucion
despues de despertar.

## 3. Frontend (Vercel)

1. En [vercel.com](https://vercel.com) entra con GitHub → **Add New → Project** → importa
   `TalentMatch`.
2. **Root Directory:** `frontend`. Framework: *Other*. El resto lo toma de `frontend/vercel.json`.
3. **Environment Variables:** `TALENTMATCH_API_URL` = `https://talentmatch-backend.onrender.com/api/v1`
   (marcala para *Production* y *Preview*).
4. **Deploy**. En **Settings → Git** confirma que la rama de produccion es `main`.
5. Si el dominio que te asigna Vercel no es `talentmatch.vercel.app`, actualiza
   `CORS_ORIGENES` en Render con tu dominio (se aceptan comodines, por ejemplo
   `https://talentmatch-*.vercel.app` para los previews).

Vercel publica solo: cada push a `main` actualiza produccion y cada push a otra rama crea un
preview. No hace falta ningun secreto en GitHub.

## Variables del backend

| Variable | Para que | Donde |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexion a Supabase | Render (secreto) |
| `JWT_SECRET` | Firma del token de sesion (minimo 32 caracteres) | Render (generado) |
| `CORS_ORIGENES` | Dominios del frontend que pueden llamar a la API | `render.yaml` |
| `FLYWAY_LOCATIONS` | Migraciones (y opcionalmente los seeds) | `render.yaml` |
| `PORT` | Puerto HTTP; lo define Render | automatico |

## Probar la imagen en local

```bash
./scripts/deploy.sh                 # construye la imagen
docker run -p 8080:8080 --env-file .env talentmatch-backend
```
