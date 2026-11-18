#!/usr/bin/env bash
# Instalacion local de TalentMatch: prepara la configuracion y descarga dependencias.
# Uso: ./scripts/setup.sh   (desde la raiz del repositorio)
set -euo pipefail
cd "$(dirname "$0")/.."

echo "== Verificando requisitos"
if ! command -v java >/dev/null 2>&1; then
  echo "Falta Java. Instala el JDK 21 (https://adoptium.net) y vuelve a ejecutar." >&2
  exit 1
fi
version=$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
if [ "$version" -lt 21 ]; then
  echo "Se requiere JDK 21 o superior (tienes Java $version)." >&2
  exit 1
fi
echo "Java $version OK"

echo "== Archivos de configuracion"
if [ ! -f conf/config.yaml ]; then
  cp conf/config.example.yaml conf/config.yaml
  echo "Creado conf/config.yaml (parametros de negocio, RNF-12)"
fi
if [ ! -f .env ]; then
  cp .env.example .env
  echo "Creado .env: COMPLETA DB_URL, DB_USER y DB_PASSWORD antes de arrancar la API"
fi

echo "== Dependencias del backend (Maven)"
(cd backend && chmod +x mvnw && ./mvnw -B -q dependency:go-offline)

echo
echo "Listo. Siguientes pasos:"
echo "  1. Completa .env con la base de datos (Supabase o PostgreSQL local)"
echo "  2. Backend:  cd backend && ./mvnw spring-boot:run"
echo "  3. Frontend: cd frontend && python -m http.server 5500   ->  http://localhost:5500"
