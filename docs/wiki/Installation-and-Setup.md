# Instalación y entorno local

## Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | **21** (por ejemplo, Temurin o Microsoft OpenJDK) | Backend. Maven **no** hace falta: el proyecto trae el wrapper `mvnw` |
| PostgreSQL | 14 o superior (en CI se usa la 16) | Base de datos local. También sirve una base de Supabase |
| Git | — | Repositorio |
| Python 3 | Opcional | Generar el informe de pruebas y servir el frontend |
| Docker | Opcional | Probar la imagen de producción |
| Un servidor estático | Live Server (VS Code) o `python -m http.server` | Frontend |

## Pasos

```bash
git clone https://github.com/jhonconmejillas-cyber/TalentMatch.git
cd TalentMatch
git checkout develop

# 1. Configuración (o ejecuta ./scripts/setup.sh, que verifica Java y crea los archivos)
cp -n conf/config.example.yaml conf/config.yaml   # parámetros de negocio
cp -n .env.example .env                           # credenciales de la BD (NO se sube a Git)
```

**2. Base de datos.** Crea una base vacía y completa `.env`:

```bash
createdb talentmatch
```
```properties
# .env
DB_URL=jdbc:postgresql://localhost:5432/talentmatch
DB_USER=postgres
DB_PASSWORD=<tu clave>
JWT_SECRET=<opcional: 32 o más caracteres>
```

**3. Backend:**

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Al arrancar, Flyway crea las tablas y carga los datos de prueba. Comprueba que responde: http://localhost:8080/api/v1/health → `{"estado":"ok"}`.

**4. Frontend:** sirve la carpeta `frontend/` en el puerto **5500**, que ya está permitido en CORS:

```bash
cd frontend
python -m http.server 5500      # o en VS Code: clic derecho en index.html → Open with Live Server
```

Abre http://localhost:5500 y entra con un usuario de prueba, por ejemplo `sofia.castro@talentmatch.com` / `talento`. La lista completa está en [Modelo de datos](Database-Model#datos-de-prueba).

## Probar la imagen Docker de producción

```bash
./scripts/deploy.sh                                     # docker build -f backend/Dockerfile -t talentmatch-backend .
docker run -p 8080:8080 --env-file .env talentmatch-backend
```

Dentro del contenedor, `localhost` no es tu máquina. Si tu PostgreSQL corre en el host, usa `host.docker.internal` en `DB_URL`.

## Reiniciar la base local desde cero

```bash
dropdb talentmatch && createdb talentmatch   # Flyway la vuelve a crear al arrancar
```

## IDE

- **IntelliJ IDEA:** abre `backend/pom.xml` como proyecto. Configura las variables de `.env` en la configuración de ejecución, o deja el `.env` en la raíz del repositorio: Spring lo importa.
- **VS Code:** Extension Pack for Java + Live Server.
- **Codificación UTF-8** en todo, porque los mensajes tienen tildes.

Si algo falla, ve a [Solución de problemas](Troubleshooting).
