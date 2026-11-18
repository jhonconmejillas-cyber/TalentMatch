# Pruebas y calidad

## Resumen

| Métrica | Valor |
|---|---|
| Pruebas | **203** JUnit 5 en 31 clases, todas en verde |
| Cobertura (JaCoCo → SonarQube) | **89.9 %** del código de producción del backend (líneas 93.8 %, ramas 76.5 %) |
| SonarQube Cloud | *Quality gate* **aprobado** · 0 bugs · 0 code smells · 0.7 % de duplicación · 17 avisos de seguridad aceptados (hashes de los datos de prueba) |
| Herramientas | JUnit 5, Mockito, Spring Boot Test (MockMvc), JaCoCo 0.8.13, SonarQube Cloud, CodeRabbit |

## Tipos de prueba

| Tipo | Cómo | Clases (número de pruebas) |
|---|---|---|
| **Unitarias de dominio** | Objetos de `model/` en memoria | `ModeloDominioTest` (18), `VacantePlanTest` (8) |
| **Unitarias de servicio** | Repositorios simulados con **Mockito** y **reloj fijo** (`Clock.fixed`) | `CalculadoraMatchScoreTest` (14), `CalculadoraMatchScorePlanTest` (10), `PostulacionServiceTest` (9), `PostulacionServicePlanTest` (8), `MatchingServiceTest` (6), `PerfilServiceTest` (5), `VacanteAutomaticaServiceTest` (6), `ParametrosServiceTest` (1), `RecalculoTest` (2) |
| **Integración de API** | Heredan de **`ApiTest`**: `@SpringBootTest` + MockMvc + **PostgreSQL real** con los seeds. Cada prueba es `@Transactional`, así que sus cambios se deshacen al terminar | `AuthControllerTest` (12), `RankingControllerTest` (11), `HabilidadControllerTest` (8), `VacanteControllerTest` (7), `ResultadoControllerTest` (6), `GestionControllerTest` (5), `PerfilControllerTest` (5), `ReporteEjecutivoTest` (5), `MeritoYAlertasTest` (4), `MatchingControllerTest` (4), `PostulacionControllerTest` (4), `ReporteRechazosTest` (4) |
| **Seguridad** | Recorren la matriz de permisos y la privacidad | `MatrizPermisosTest` (19), `ProteccionDatosTest` (9) |
| **Persistencia y configuración** | Mapeo JPA, triggers, conexión y propiedades | `MapeoJpaTest` (6), `BitacoraCalculoTest` (2), `ConexionBaseDatosTest` (1), `ConfiguracionTest` (1), `TalentmatchApplicationTests` (1) |
| **Rendimiento** | 50 usuarios concurrentes; ranking de 20 candidatos en menos de 2 s | `CargaConcurrenteTest` (2), `RankingControllerTest` |

### `ApiTest`: base de las pruebas de integración

```java
class MiPruebaTest extends ApiTest {
    @Test
    void ejemplo() throws Exception {
        mvc.perform(get("/api/v1/vacantes").header("Authorization", login(EMPLEADO_SOFIA)))
           .andExpect(status().isOk());
    }
}
```

`login(correo)` inicia sesión con la contraseña de los seeds (`CONTRASENA_SEED = "talento"`) y devuelve `Bearer <token>`. Hay constantes para los usuarios de prueba: `ADMIN`, `RRHH`, `LIDER`, `MANAGER_CARLOS`, `MANAGER_DIANA`, `EMPLEADO_JUAN` y `EMPLEADO_SOFIA`.

Trampas conocidas:
- Como todo ocurre en una sola transacción, si una prueba verifica con `JdbcTemplate` algo que hizo JPA, hay que llamar a `em.flush()` (y a veces a `em.clear()`) antes de consultar.
- Hibernate ejecuta los INSERT antes que los UPDATE al hacer *flush*. Por eso `liberarAsignacion` usa `saveAndFlush`: si no, choca con el índice `ux_asignacion_vigente`.

## Plan de pruebas del taller (CP01 a CP26)

Para el **Taller de Pruebas Unitarias con JUnit y Métricas con SonarQube** se diseñó un plan formal sobre 3 componentes clave:

| Componente | Clase de prueba | Normal | Borde | Negativa | Negocio |
|---|---|---|---|---|---|
| `CalculadoraMatchScore` | `CalculadoraMatchScorePlanTest` | 4 | 3 | 2 | 1 |
| `Vacante` | `VacantePlanTest` | 2 | 2 | 3 | 1 |
| `PostulacionService` | `PostulacionServicePlanTest` | 2 | 2 | 3 | 1 |

- **Fuente única:** `docs/pruebas/plan-de-pruebas.csv` (separado por `|`). La usan el documento `docs/pruebas/Plan-de-Pruebas.md` y el informe del pipeline.
- **Trazabilidad:** cada prueba tiene `@DisplayName("CPxx | Tipo | Descripción")` y `@Tag("plan-pruebas")`. Surefire está configurado (`usePhrasedTestCaseMethodName`) para que el XML lleve ese nombre y el informe pueda cruzarlo con el CSV.
- **Estilo:** patrón Arrange-Act-Assert (comentarios `// Arrange`, `// Act`, `// Assert`) y principios F.I.R.S.T.
- **Informe del taller:** `docs/pruebas/Informe-Taller-Pruebas.md`, más el documento Word en formato APA que se entregó.

**Para agregar un caso al plan:** agrega la fila en el CSV con el siguiente `CPxx`, escribe la prueba con ese `@DisplayName` y regenera la tabla de `Plan-de-Pruebas.md`. Si una fila del CSV no tiene prueba, el informe la marca como **"No ejecutado"**.

## Ejecutar las pruebas en local

Las pruebas de integración necesitan PostgreSQL:

```bash
# 1. Una base vacía (Flyway crea el esquema y carga los seeds)
createdb talentmatch_test
# 2. Variables (o en el archivo .env)
export DB_URL=jdbc:postgresql://localhost:5432/talentmatch_test DB_USER=postgres DB_PASSWORD=<tu clave>
# 3. Ejecutar
cd backend
./mvnw verify                            # todo + cobertura en target/site/jacoco/index.html
./mvnw test -Dtest='*PlanTest'           # solo el plan (no requiere BD)
./mvnw test -Dtest=RankingControllerTest # una clase
# 4. Informe HTML como el del pipeline (desde la raíz del repositorio)
python3 scripts/reporte_pruebas.py       # → informe-pruebas/reporte-pruebas.html
```

## Cobertura (JaCoCo)

`pom.xml` tiene el plugin `jacoco-maven-plugin` 0.8.13 con dos ejecuciones:
- `prepare-agent` instrumenta las pruebas.
- `report`, en la fase `verify`, genera `target/site/jacoco/jacoco.xml` y el HTML.

**Qué se excluye de la cobertura** (`sonar.coverage.exclusions`):
- `backend/src/test/**`
- `frontend/**`: no tiene lógica de negocio.
- `scripts/**`
- `TalentmatchApplication`
- Los DTO
- Las clases `*Properties`

El scanner nuevo de SonarQube analiza **todo** el repositorio e ignora `sonar.sources` y `sonar.tests`. Sin esas exclusiones contaba las pruebas y el frontend como código por cubrir, y la cobertura bajaba a 43 %.

## SonarQube Cloud

- Organización `jhonconmejillas-cyber`, proyecto `jhonconmejillas-cyber_TalentMatch` (público): https://sonarcloud.io/project/overview?id=jhonconmejillas-cyber_TalentMatch
- **Método de análisis:** desde el CI (el análisis automático está **desactivado**). Requiere el secreto `SONAR_TOKEN`.
- Configuración en `sonar-project.properties`: organización, clave del proyecto, binarios Java, reportes de Surefire, `jacoco.xml`, `sonar.python.version=3.12` y exclusiones de cobertura.
- Aviso esperado: "Data Dictionary is not configured for the PLSQL analyzer". Es informativo: Sonar trata los `.sql` como PL/SQL de Oracle y nuestras migraciones son de PostgreSQL. No afecta.

### Problemas que encontró y cómo se resolvieron

| # | Hallazgo | Solución |
|---|---|---|
| 1 | DOM XSS en `layout.js` | Encabezado construido con la API del DOM |
| 2 | 17 promesas sin manejar en el frontend | `ejecutar(promesa)` en `ui.js` |
| 3 | Cobertura no medida | JaCoCo + análisis desde el CI |
| 4 | Contraseña en `ci.yml` | Base del CI con `trust` |
| 5 | `[` en lugar de `[[` en `setup.sh` | `[[ ... ]]` |
| 6 | Hashes BCrypt en los seeds | Aceptado (datos de demostración) |
| 7 | Acción sin fijar a un SHA y *path traversal* en el script del informe | SHA fijo y rutas fijas |
| 8 | 3 bugs: NPE en `DecisionService`, fechas sin zona en `RankingService` y `SecureRandom` recreado en `TokenService` | `requireNonNullElse`, `ChronoUnit` con zona y `SecureRandom` estático |
| 9 | Cobertura mal medida (43 %) | `sonar.coverage.exclusions` |

## CodeRabbit (revisión automática de PRs)

- App instalada en el repositorio, configurada con `.coderabbit.yaml`: idioma español, perfil *chill*, revisa los PRs hacia `develop`, `main` y `revision`, y no repite la revisión en cada push.
- El plan permite **10 revisiones por hora**. Para pedir una revisión manual, comenta `@coderabbitai review` en el PR.
- En la revisión de los PRs del proyecto encontró y se corrigieron, en el modelo de dominio:
  - `liberar` sin validar el motivo ni la fecha.
  - `agregarAsignacion` sin validar el dueño ni que haya una sola vigente.
  - Activación de una vacante antes de su ventana de RN-03.
  - `programarActivacion(null)`.
  - `equals` y `hashCode` incoherentes en `Habilidad`.
  - `esMismoPuestoQue` con un proyecto sin id.
  - Ver [Historial](Project-History#revisión-con-coderabbit).
