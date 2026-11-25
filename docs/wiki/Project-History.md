# Historial del proyecto

Registro, en orden, de todo lo que se construyó y de los cambios importantes posteriores, con sus Pull Requests. Para el detalle técnico de cada uno, abre el PR en GitHub.

## Fase 0: especificación (antes del código)

Documentación formal en `docs/`:
- 30 reglas de negocio.
- 33 requerimientos funcionales y 13 no funcionales.
- 10 historias de usuario.
- 25 casos de uso.
- Diagrama de clases.
- Wiki divulgativa.

Se confirmaron los pendientes PEN-01 (alerta a los 10 días), PEN-02 (costo de rotación) y PEN-03 (sin Career Impact Score). Se creó la estructura de carpetas y la plantilla `conf/config.example.yaml`.

## Fase 1: implementación del backlog (PRs #36 a #60)

Cada issue se implementó en su rama `feature/<n>-...`, con un PR hacia `develop` que lo cierra.

| PR | Issue | Contenido |
|---|---|---|
| #36 | S1-01 (#15) | Estructura Maven/Spring del backend |
| #37 | S1-02 (#14) | Esqueleto Spring Boot y endpoint `/health` |
| #38 | S1-03 (#33) | Clases del diagrama de clases en Java (`model/`) |
| #39 | S1-04 (#17) | Conexión a PostgreSQL/Supabase con variables de entorno |
| #40 | S1-05 (#18) | Migración Flyway V1, datos de prueba V1_1 y mapeo JPA |
| #41 | S1-06 (#21) | GitHub Actions: compilar y ejecutar JUnit en cada PR |
| #42 | S1-07 (#16) | Frontend: estructura, cliente `api.js` y login |
| #43 | S1-08 (#19) | Registro, login con BCrypt y token, permisos por rol |
| #44 | S1-09 (#20) | Perfil profesional de punta a punta |
| #45 | S1-10 (#22) | Dockerfile y publicación del frontend (entonces en Cloudflare Pages) |
| #46 | S1-11 (#23) | Guía de levantamiento local |
| #47 | S2-01 (#27) | HU-007: avalar las habilidades del equipo |
| #48 | S2-02 (#24) | Publicar, buscar y filtrar vacantes |
| #49 | S2-03 (#25) | Backend en Cloudflare Containers (reemplazado después en el #66) |
| #50 | S3-01 (#26) | Cálculo del Puntaje Final con bitácora obligatoria |
| #51 | S3-02 (#28) | HU-008: postulación con reglas claras |
| #52 | S3-03 (#29) | HU-009: vacante automática al dejar un puesto |
| #53 | S4-01 (#8) | HU-001: ranking inmutable y decisión del manager |
| #54 | S4-02 (#11) | HU-004: mérito y alertas a RR.HH. (V2 `rechazo_directo`) |
| #55 | S4-03 (#10) | HU-003: resultado anónimo para el rechazado |
| #56 | S5-01 (#9) | HU-002: reporte mensual de patrones de rechazo (V3) |
| #57 | S5-02 (#30) | HU-010: reporte top 10 |
| #58 | S5-03 (#12) | HU-005: rendimiento (índices V4, puntaje precalculado, carga concurrente) |
| #59 | S5-04 (#13) | HU-006: protección de datos (matriz de permisos, bitácora de accesos V5, sesión por inactividad) |
| #60 | — | Corrección: el ranking de una vacante cerrada salía vacío; ahora muestra el resultado final |

Al terminar: **177 pruebas** en verde y verificación de los flujos principales en el navegador.

## Revisión con CodeRabbit

Se instaló **CodeRabbit** para revisar los PRs. Como los 25 PRs ya estaban fusionados, se creó la rama `revision`, que parte del estado anterior al #36, y se volvieron a aplicar los PRs con los mismos commits originales.

| PR | Qué | Hallazgos de CodeRabbit aplicados |
|---|---|---|
| #61 (recrea el #36) | Estructura | README: alcance de RN-18 en el paquete `dto` |
| #62 (recrea el #37) | Esqueleto | README: `cp -n` para no sobrescribir `conf/config.yaml` |
| #63 (recrea el #38) | Modelo de dominio | 8 correcciones (ver abajo); 2 sugerencias respondidas como no aplicables |
| #64 (recrea el #43) | Autenticación | — (la revisión se interrumpió) |

Correcciones del modelo que salieron de esta revisión:
- `AsignacionProyecto.liberar` exige motivo y fecha.
- `Empleado.agregarAsignacion` valida que la asignación sea del propio empleado y que solo haya una vigente.
- `Vacante.activar` respeta la ventana de RN-03 y `programarActivacion` exige una fecha.
- `Habilidad` usa la misma equivalencia en `equals` y en `hashCode`.
- `Vacante.esMismoPuestoQue` reconoce el mismo proyecto aunque no tenga id.

Como cada revisión tardaba mucho (y el plan permite 10 por hora), **se decidió dejar de revisar PR por PR**. Los demás cambios se aplicaron directo a `revision`, se verificaron las 177 pruebas y se integró todo a `develop` en el **#65**. CodeRabbit sigue activo para los PRs nuevos.

## Despliegue gratuito: Vercel + Render + Supabase (PR #66)

Cloudflare Containers exige un plan de pago, y Vercel no ejecuta Java. Por eso:
- **Frontend → Vercel:** `frontend/vercel.json` + `scripts/generar-ambiente.mjs` + la variable `TALENTMATCH_API_URL`.
- **Backend → Render:** el mismo `backend/Dockerfile`, más `render.yaml`. `server.port` lee `${PORT}` y CORS acepta patrones con comodín.
- **Se eliminaron** `deploy/cloudflare/` y los workflows `frontend.yml` y `deploy-backend.yml`.
- **Configuración real en producción:** Supabase con el Session pooler (`aws-1-us-west-2`), un Web Service de Render llamado `TalentMatch` (`talentmatch-n5kp.onrender.com`) y el proyecto `talentmatch-frontend` en Vercel (`talentmatch-frontend-puce.vercel.app`).
- **Error durante la configuración:** `UnknownHostException: ...pooler.supabase.com`. Se había pegado el ejemplo con `...` en `DB_URL` en lugar del host real (ver [Solución de problemas](Troubleshooting)).

## Versión 1.0.0 (PRs #67 y #68)

- **#67:** `release/1.0.0` → `main`, con el CHANGELOG del MVP.
- Tag **`v1.0.0`** y release en GitHub.
- **#68:** el CHANGELOG vuelve a `develop`.

## Contraseña de los usuarios de prueba (PRs #69 y #70)

Se cambió la contraseña de los 16 usuarios de prueba de `TalentMatch2026` a **`talento`**:
- Se creó la migración nueva `database/seeds/V5_1__clave_usuarios_prueba.sql` y se actualizaron las pruebas (`ApiTest`, `CargaConcurrenteTest`) y los README.
- **No se editó `V1_1`**: Flyway ya la había aplicado en producción y cambiar su checksum habría impedido arrancar. Incluso un comentario cuenta.
- **#69** fue a `develop` y **#70** a `main`, para que Render lo desplegara.

## Taller de pruebas y métricas (PRs #71 a #74)

Para el *Taller de Pruebas Unitarias con JUnit y Métricas con SonarQube*:

| PR | Contenido |
|---|---|
| #71 | Plan de pruebas CP01 a CP26 (`docs/pruebas/`) y 3 clases `*PlanTest`. Pipeline con informe HTML/JSON como artefacto y en el *Summary*, JaCoCo y paso de SonarQube Cloud. Corrección de los problemas de Sonar: XSS en `layout.js`, 17 promesas sin manejar, contraseña en `ci.yml`, `[[` en `setup.sh`, acción fijada a su SHA y rutas fijas en el script del informe |
| #72 | `sonar.python.version=3.12` |
| #73 | 3 bugs de Sonar (NPE en `DecisionService`, `SecureRandom` reutilizable en `TokenService`) y `sonar.coverage.exclusions` para medir la cobertura sobre el código de producción |
| #74 | Días del ciclo de RN-12 calculados con zona horaria (`RankingService`) |

**Resultado:** 203 pruebas, 89.9 % de cobertura, 0 bugs y *quality gate* aprobado. Se entregó el informe en Word con normas APA (`docs/pruebas/Informe-Taller-Pruebas.md` es su versión Markdown).

## Configuración manual hecha fuera del repositorio

| Dónde | Qué |
|---|---|
| GitHub → Branches | Check **"Compilar y probar backend"** obligatorio en `develop` y `main` |
| GitHub → Secrets | `SONAR_TOKEN` |
| GitHub → Apps | CodeRabbit instalado solo en TalentMatch |
| SonarQube Cloud | Proyecto importado; **Automatic Analysis desactivado** (el análisis viene del CI) |
| Supabase | Proyecto y contraseña de la base (la guarda el equipo) |
| Render | Web Service Docker con sus variables de entorno |
| Vercel | Proyecto `talentmatch-frontend`, Root Directory `frontend`, `TALENTMATCH_API_URL` |

## Pendientes

- **Validación del Product Owner** de las historias sobre producción.
- **Vulnerabilidades aceptadas:** los hashes de los datos de prueba (17). Antes de un uso real, quitar los seeds de producción y crear usuarios reales.
- **Commits del equipo:** los cambios de esta etapa se subieron desde la cuenta de Jhon; los demás integrantes deben aportar con sus cuentas.
- **CI:** migrar `actions/setup-java@v4` → `@v5` (aviso de *deprecation*, no bloquea).
