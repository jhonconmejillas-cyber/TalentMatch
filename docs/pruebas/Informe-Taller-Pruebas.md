# TalentMatch: informe del taller de pruebas unitarias y métricas

| Campo | Valor |
|---|---|
| **Taller** | Pruebas Unitarias con JUnit y Métricas con SonarQube (Fundamentos de Ingeniería de Software) |
| **Proyecto** | TalentMatch: plataforma de movilidad interna de talento (Spring Boot 4 + Java 21, HTML/JS, PostgreSQL) |
| **Repositorio** | https://github.com/jhonconmejillas-cyber/TalentMatch |
| **SonarQube Cloud** | https://sonarcloud.io/project/overview?id=jhonconmejillas-cyber_TalentMatch |

> Las líneas **📸 Captura** marcan dónde va cada captura de pantalla que pide el punto 7.

---

## 1. Plan de pruebas

El plan completo está en [`Plan-de-Pruebas.md`](Plan-de-Pruebas.md). Tiene 26 casos (CP01–CP26)
sobre 3 componentes clave:

| Componente | Métodos probados | Normal | Borde | Negativa | Negocio |
|---|---|---|---|---|---|
| `CalculadoraMatchScore` | `calcular`, porcentaje, `ajusteSenioridad`, redondeo, `Resultado.detalle` | 4 | 3 | 2 | 1 |
| `Vacante` | `activar`, `cerrar`, `programarActivacion`, `debeActivarse`, `descartar`, `esMismoPuestoQue`, `calcularRanking` | 2 | 2 | 3 | 1 |
| `PostulacionService` | `postularse`, `elegibilidad`, `revisarReglas`, `cancelarOtrasPostulaciones`, `misPostulaciones` | 2 | 2 | 3 | 1 |

Ejemplo con la plantilla del taller:

| Caso de prueba | Descripción | Entrada de datos | Tipo de prueba | Resultados esperados | Resultados reales |
|---|---|---|---|---|---|
| CP15 | Puntaje Final igual al umbral | Puntajes 65.0 y 64.9; umbral 65 | Borde | Solo entra la postulación de 65.0 | Solo entra la de 65.0 ✅ |
| CP20 | Postularse con un empleado inexistente | idEmpleado = 999 | Negativa | `RecursoNoEncontradoException`; no se guarda nada | Lanza la excepción y no guarda ✅ |
| CP24 | Al aceptar una postulación, las demás se cancelan (RN-09) | Postulaciones activas A (aceptada) y B | Negocio | B queda `CANCELADA_POR_ROTACION` | B cancelada, A aceptada ✅ |

## 2. Commits de cada participante

📸 **Captura:** GitHub → *Insights → Contributors*, o la lista de commits de la rama `develop`.

## 3. Implementación de las pruebas (código)

Las pruebas están en `backend/src/test/java/co/edu/javeriana/talentmatch/`:

- `service/CalculadoraMatchScorePlanTest.java`
- `model/VacantePlanTest.java`
- `service/PostulacionServicePlanTest.java` (repositorios simulados con Mockito y reloj fijo)

Todas siguen el patrón **Arrange - Act - Assert** y los principios **F.I.R.S.T**. Se explican en
la sección 2 del plan.

📸 **Captura:** una prueba de cada clase, por ejemplo `cp15_umbralExacto` y `cp24_cancelacionEnCascada`.

## 4. Pruebas ejecutándose

- **Local:** `cd backend && ./mvnw test -Dtest='*PlanTest'` → 26 pruebas, 0 fallos.
  La suite completa (`./mvnw verify`) da **203 pruebas, 0 fallos**.
- **Pipeline:** `.github/workflows/ci.yml` corre en cada PR hacia `develop` o `main` y en cada
  push a `develop`, `main`, `release/**` y `hotfix/**`. Genera el informe (`python3 scripts/reporte_pruebas.py`)
  y lo publica siempre como artefacto **informe-pruebas**. Contiene `reporte-pruebas.html`,
  `reporte-pruebas.json`, los XML de Surefire y la cobertura de JaCoCo. El resumen también
  aparece en la pestaña *Summary* de la ejecución.

📸 **Capturas:**
1. La consola con `Tests run: 203, Failures: 0, Errors: 0`.
2. GitHub → *Actions* → la ejecución de **CI** en verde, con el resumen "Informe de pruebas".
3. El archivo `reporte-pruebas.html` abierto (descargado del artefacto **informe-pruebas**).

## 5. Qué se logró identificar o prevenir con las pruebas

- **Límites de las reglas de negocio:** los casos de borde fijan el comportamiento exacto en cada
  límite. Si alguien cambia `>=` por `>`, una prueba falla.
  - El umbral de 65 (CP15).
  - La ventana de 24 h de RN-03 (CP14).
  - Los 6 meses de RN-07 y RN-10 (CP22, CP23).
- **Estados inválidos:**
  - Cerrar una vacante que no está activa (CP12).
  - Descartar una vacante cerrada (CP18).
  - Programar una activación sin fecha (CP13). Antes dejaba la vacante pendiente para siempre;
    se corrigió en el modelo a partir de la revisión de código.
- **Neutralidad del cálculo (RNF-09 y RN-20):** una habilidad sin aval no suma puntaje (CP08), y
  la fórmula no recibe datos personales del empleado.
- **Entradas nulas o inexistentes:**
  - Datos nulos en el cálculo (CP06, CP07).
  - Empleados que no existen (CP20).
  - Postulaciones repetidas (CP21).
- **Efectos en cascada (RN-09):** al aceptar una postulación, las demás del empleado se cancelan
  y la aceptada no cambia (CP24).

## 6. Evaluación de calidad con SonarQube Cloud

### 6.1 Integración

- El análisis corre en el pipeline (`SonarSource/sonarqube-scan-action`) con la configuración de
  [`sonar-project.properties`](../../sonar-project.properties).
- Analiza el backend Java, el frontend JS, el SQL y los scripts.
- Importa los resultados de las pruebas (Surefire) y la **cobertura de JaCoCo**
  (`backend/target/site/jacoco/jacoco.xml`).

📸 **Captura:** el dashboard *Overview* de SonarQube Cloud, con Cobertura, Bugs (Reliability),
Vulnerabilidades (Security), Code Smells (Maintainability) y Duplicaciones.

### 6.2 Resultado del primer análisis

| Métrica | Primer análisis (automático) |
|---|---|
| Security | 20 issues |
| Reliability | 20 issues |
| Maintainability | 0 issues |
| Duplicaciones | 1.0 % de 3.8 k líneas |
| Cobertura | Sin medir (el análisis automático no mide cobertura) |

### 6.3 Problemas relevantes y soluciones implementadas

| # | Problema detectado por SonarQube | Severidad | Solución implementada |
|---|---|---|---|
| 1 | **DOM XSS** en `frontend/js/layout.js`: el encabezado se armaba con `innerHTML` usando datos de la sesión guardados en el navegador (por ejemplo, el rol en el `href`). | Security · Blocker | El encabezado se arma con la API del DOM (`createElement`, `textContent`, `href`). Ningún dato de la sesión pasa por `innerHTML`. |
| 2 | **Promesas sin manejar** (`javascript:S9383`) en 14 páginas: 17 llamadas a funciones `async` sin `await` ni `.catch`. Un error inesperado se perdía en silencio. | Reliability · Medium | Nueva función `ejecutar(promesa)` en `frontend/js/ui.js`, que registra cualquier error no controlado. Se usa en las 17 llamadas. |
| 3 | **Baja cobertura (cobertura no medida):** SonarQube no recibía datos de cobertura. | Coverage | Se agregó **JaCoCo** al `pom.xml`. El pipeline envía el reporte a SonarQube. La cobertura medida es **93.6 % de líneas** y **74.4 % de ramas**. |
| 4 | **Contraseña escrita en el pipeline** (`yaml:S2068`) en `ci.yml`, para la base de datos temporal de pruebas. | Security · Medium | La base de prueba del CI usa `POSTGRES_HOST_AUTH_METHOD: trust`: solo existe durante el job y ya no hay ninguna contraseña en el repositorio. |
| 5 | **`[` en lugar de `[[`** en `scripts/setup.sh` (`shell:S7688`). | Reliability · High | Se cambió a `[[ ... ]]`, que evita errores con variables vacías o con espacios. |
| 7 | **Revisión del PR del taller** (segundo análisis): la acción de Sonar sin fijar a un *commit SHA* (`githubactions:S7637`) y el script del informe aceptaba rutas por línea de comandos (*path traversal*, `pythonsecurity:S8707`). | Security | La acción se fija a su SHA completo (`sonarqube-scan-action@e805d87…`, v8.4.1) y el script usa rutas fijas dentro del repositorio, sin argumentos. |
| 6 | **Hashes BCrypt en los datos de prueba** (`secrets:S8215`) en `database/seeds/`. | Security · Blocker | Son los usuarios de **prueba** de la demo, no cuentas reales. No se pueden borrar de `V1_1` porque Flyway ya aplicó ese archivo en producción. **Propuesta:** en producción no cargar `database/seeds` (variable `FLYWAY_LOCATIONS` sin la carpeta de seeds) y marcar estos issues en SonarQube como *Accepted* con la justificación "datos de prueba". |

📸 **Captura:** la pestaña *Issues* antes y después de los cambios (el segundo análisis corre al
fusionar el PR del taller).

## 7. Reflexión

Las pruebas unitarias convierten las reglas de negocio en verificaciones automáticas. En
TalentMatch una regla mal implementada tiene consecuencias directas sobre las personas:

- Un umbral comparado con `>` en vez de `>=` deja fuera a un candidato con 65 puntos.
- Un error en la cancelación en cascada deja a un empleado en dos rankings a la vez.

Sin pruebas, estos errores solo aparecen cuando un usuario los sufre.

Los casos de borde y negativos fueron los más valiosos. Obligaron a decidir qué debe pasar en cada
límite y a escribirlo. En un caso mostraron un hueco real: programar una activación sin fecha
dejaba la vacante bloqueada.

SonarQube complementa las pruebas. Las pruebas comprueban que el código hace lo que debe; Sonar
encuentra lo que nadie estaba mirando, como el XSS del encabezado, las promesas sin manejar o la
contraseña en el pipeline. Además, mide la cobertura y muestra qué partes quedan sin probar.

En un proyecto de mayor escala, con más desarrolladores y despliegues frecuentes, este pipeline es
lo que permite cambiar código con confianza. Cada PR ejecuta automáticamente las pruebas y el
análisis, y no se fusiona si algo falla. El costo de un error detectado en el PR es minutos. En
producción sería una decisión de movilidad injusta, una brecha de seguridad o la pérdida de
confianza en el sistema.
