#!/usr/bin/env bash
# Construye la imagen Docker del backend. La publicacion la hacen los workflows de
# GitHub Actions (frontend -> Cloudflare Pages, backend -> Cloudflare Containers).
set -euo pipefail
cd "$(dirname "$0")/.."
docker build -f backend/Dockerfile -t talentmatch-backend .
echo "Imagen talentmatch-backend lista. Prueba local: docker run -p 8080:8080 --env-file .env talentmatch-backend"
