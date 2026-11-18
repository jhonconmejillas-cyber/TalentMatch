# Despliegue del backend en Cloudflare Containers

```
Usuario → Cloudflare Pages (frontend) → Worker talentmatch-api → Contenedor Spring Boot → Supabase
```

| Archivo | Para qué |
|---|---|
| `wrangler.jsonc` | Worker + contenedor construido con `backend/Dockerfile`. |
| `src/index.js` | Worker que reenvía cada petición al contenedor y le pasa los secretos como variables de entorno. |
| `../../.github/workflows/deploy-backend.yml` | Despliega al fusionar en `main`. |

## Configuración única

1. Cuenta de Cloudflare con el plan **Workers Paid** (requisito de Containers).
2. API token con permisos *Workers Scripts: Edit*, *Containers: Edit* y *Account Settings: Read*.
3. En GitHub → Settings → Secrets and variables → Actions:
   - Secretos: `CLOUDFLARE_API_TOKEN`, `CLOUDFLARE_ACCOUNT_ID`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres).
   - Variable: `DESPLEGAR_BACKEND=true` (mientras no exista, el workflow se omite).
4. Ajustar `CORS_ORIGENES` en `wrangler.jsonc` con la URL real de Cloudflare Pages.
5. Poner la URL del Worker (`https://talentmatch-api.<cuenta>.workers.dev/api/v1`) en las
   variables `API_URL_PRODUCCION` / `API_URL_PREVIEW` del frontend.

## Despliegue manual

```bash
cd deploy/cloudflare
npm install
npx wrangler secret put DB_URL        # repetir con DB_USER, DB_PASSWORD y JWT_SECRET
npx wrangler deploy                   # necesita Docker para construir la imagen
```

Comprobación: `curl https://talentmatch-api.<cuenta>.workers.dev/api/v1/health` → `{"estado":"ok"}`.
