#!/usr/bin/env bash
# Ejecuta la bateria de pruebas JUnit del backend (unitarias e integracion).
# Las de integracion necesitan una base PostgreSQL: DB_URL, DB_USER y DB_PASSWORD
# en el entorno o en el archivo .env de la raiz.
set -euo pipefail
cd "$(dirname "$0")/../backend"
./mvnw -B verify
echo "Reportes: backend/target/surefire-reports/"
