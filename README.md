# TalentMatch

**Plataforma de movilidad interna para ingenieros** — MVP, 4 meses (16 semanas).

TalentMatch conecta ingenieros con proyectos y vacantes dentro de su **propia empresa**, usando
una dinámica inspirada en Tinder pero basada en **reglas lógicas simples y explicables, sin IA**.

| | |
|---|---|
| **Curso** | Fundamentos de Ingeniería de Software (FIS 2610) — Pontificia Universidad Javeriana |
| **Versión** | MVP |
| **Estado** | En desarrollo |
| **Wiki** | https://github.com/jhonconmejillas-cyber/TalentMatch/wiki |

---

## El problema

Cuando un ingeniero se aburre o siente que dejó de aprender, **no pide un cambio de proyecto**:
busca otra empresa. La organización pierde talento conocido y paga entre 4.000 y 9.000 USD para
reemplazarlo.

## La solución

| Pilar | Qué hace |
|---|---|
| **Visibilidad** | El empleado ve todas las vacantes internas relevantes para su carrera, incluso en otras áreas. |
| **Transparencia** | Cada puntaje es explicable: `Puntaje Final = Match Score`. Sin caja negra. |
| **Argumento económico** | El manager ve cuánto cuesta perder a esa persona frente a cuánto cuesta moverla. |
| **Decisión por mérito** | El ranking es inmutable y ningún dato personal entra al cálculo. |

---

## 📚 Documentación del proyecto

Toda la especificación vive en **[`docs/`](docs/)**:

| Documento | Contenido |
|---|---|
| **[Reglas de Negocio](docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md)** | RN-01 … RN-30. **Fuente única de verdad.** |
| **[Requerimientos](docs/requerimientos/RF-RNF-TalentMatch.md)** | 32 RF activos y 13 RNF, trazados a las reglas de negocio. |
| **[Historias de Usuario](docs/historias-usuario/HU-TalentMatch.md)** | 10 historias con criterios de aceptación, DoD y subtareas. |
| **[Casos de Uso](docs/casos-uso/CU-TalentMatch.md)** | CU_01 … CU_25 (24 activos) en formato estándar, con matriz de trazabilidad. |
| **[Copia del wiki](docs/wiki/)** | Instantánea de las páginas del wiki, versionada junto al código. |

```
Reglas de Negocio (RN)  ->  Requerimientos (RF/RNF)  ->  Casos de Uso (CU)
        ^                                                      |
        +------------------ Historias de Usuario (HU) ---------+
```

Ante cualquier contradicción entre documentos, **prevalecen las Reglas de Negocio**.

---

## 🗂️ Estructura del repositorio

```
TalentMatch/
├── docs/                    Especificación completa del proyecto
│   ├── reglas-negocio/      RN-01 ... RN-30
│   ├── requerimientos/      RF-01 ... RF-33 · RNF-01 ... RNF-13
│   ├── historias-usuario/   HU-001 ... HU-010
│   ├── casos-uso/           CU_01 ... CU_25
│   ├── diagramas/           Diagramas de casos de uso, clases y despliegue
│   └── wiki/                Copia versionada de las páginas del wiki
├── backend/                 API REST en Java 21 + Spring Boot (Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/co/edu/javeriana/talentmatch/
│       │   ├── controller/  Endpoints HTTP (capa 2)
│       │   ├── service/     Reglas de negocio (capa 3)
│       │   ├── repository/  Interfaces JPA (capa 4)
│       │   ├── model/       Entidades del diagrama de clases
│       │   ├── dto/         Contratos JSON (anonimato RN-18)
│       │   ├── security/    Login, token y permisos por rol
│       │   └── config/      Lectura de conf/config.yaml
│       └── test/java/       Pruebas JUnit
├── frontend/                HTML + CSS + JavaScript sin framework
│   ├── index.html           Login
│   ├── pages/  js/  css/
├── database/                Esquema, migraciones y datos de prueba
│   ├── migrations/
│   └── seeds/
├── conf/                    Pesos, umbrales y plazos configurables (RNF-12)
├── scripts/                 Instalación, pruebas y despliegue
└── .github/                 Plantillas de issues y pull requests
```

---

## 🚀 Levantar el proyecto en local

### Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| **JDK** | 21 | Compilar y ejecutar el backend. Maven **no** hace falta instalarlo: el proyecto trae `mvnw`. |
| **PostgreSQL** | 14+ (o un proyecto de Supabase) | Base de datos. |
| **Python 3** (o la extensión *Live Server* de VS Code) | cualquiera | Servir el frontend estático. |
| Git Bash (en Windows) | — | Ejecutar los scripts de `scripts/`. |

### Pasos

```bash
git clone https://github.com/jhonconmejillas-cyber/TalentMatch.git
cd TalentMatch
./scripts/setup.sh            # crea conf/config.yaml y .env, descarga dependencias
```

1. **Base de datos.** Completa `.env` con la conexión:
   - Supabase: *Project Settings → Database → Connection string → JDBC* (session pooler, agrega `?sslmode=require`).
   - PostgreSQL local: crea una base vacía (`createdb talentmatch`) y usa `DB_URL=jdbc:postgresql://localhost:5432/talentmatch`.
2. **Backend** (aplica las migraciones y los datos de prueba al arrancar):
   ```bash
   cd backend && ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
   curl http://localhost:8080/api/v1/health  # {"estado":"ok"}
   ```
3. **Frontend**, en otra terminal:
   ```bash
   cd frontend && python -m http.server 5500
   ```
   Abre http://localhost:5500 e inicia sesión con un usuario de prueba (contraseña `TalentMatch2026`):

   | Rol | Correo |
   |---|---|
   | Empleado | `juan.ramirez@talentmatch.com` |
   | Manager | `carlos.perez@talentmatch.com` |
   | RR.HH. | `rrhh@talentmatch.com` |
   | Líder Ejecutivo | `lider@talentmatch.com` |
   | Administrador | `admin@talentmatch.com` |

### Pruebas

```bash
./scripts/test.sh   # JUnit (unitarias + integración contra la base de .env)
```

> Los parámetros del algoritmo (pesos 0.7/0.3, umbral 65 %, ciclo de 3 días, plazos de 6 meses
> y 24 horas) se editan en `conf/config.yaml` y se aplican al reiniciar la API, **sin recompilar** (RNF-12).

### Más información

- Arquitectura de 4 capas, API y despliegue: [Technical Architecture](https://github.com/jhonconmejillas-cyber/TalentMatch/wiki/Technical-Architecture) ([copia local](docs/wiki/Technical-Architecture.md))
- Ramas, PRs y commits: [Development Workflow](https://github.com/jhonconmejillas-cyber/TalentMatch/wiki/Development-Workflow) y [CONTRIBUTING.md](CONTRIBUTING.md)
- Detalle de cada parte: [backend/](backend/README.md) · [frontend/](frontend/README.md) · [database/](database/README.md)

---

## 👥 Equipo

| Integrante | Rol(es) | GitHub |
|---|---|---|
| Carlos Camacho | Scrum Master / DevOps Engineer | [@charly-31](https://github.com/charly-31) |
| Jhon Mejia | Product Owner / Configuration Manager | [@jhonconmejillas-cyber](https://github.com/jhonconmejillas-cyber) |
| Juan Maldonado | Sprint Planner / QA Lead | [@camilo058](https://github.com/camilo058) |

---

## 📄 Licencia

[MIT](LICENSE)
