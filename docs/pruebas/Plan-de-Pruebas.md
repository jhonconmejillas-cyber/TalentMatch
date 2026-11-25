# TalentMatch: plan de pruebas unitarias

| Campo | Valor |
|---|---|
| **Documento** | Plan de pruebas unitarias (Taller de Pruebas Unitarias con JUnit y Métricas con SonarQube) |
| **Herramientas** | JUnit 5, Mockito, JaCoCo, GitHub Actions, SonarQube Cloud |
| **Fuente única** | [`plan-de-pruebas.csv`](plan-de-pruebas.csv): la usan este documento y el informe del pipeline |

## 1. Análisis del proyecto

Se eligieron los **3 componentes con más lógica de negocio** de TalentMatch, que se pueden probar
de forma aislada (sin base de datos ni servidor):

| # | Componente | Por qué es clave | Métodos o procesos probados |
|---|---|---|---|
| 1 | `CalculadoraMatchScore` | Calcula el puntaje que ordena el ranking. Un error aquí cambia a quién se elige. | `calcular`, porcentaje de habilidades, `ajusteSenioridad`, redondeo, `Resultado.detalle` |
| 2 | `Vacante` | Controla el ciclo de vida de la vacante y el ranking inmutable. | `activar`, `cerrar`, `programarActivacion`, `debeActivarse`, `descartar`, `esMismoPuestoQue`, `calcularRanking` |
| 3 | `PostulacionService` | Aplica las reglas de quién puede postularse y cuándo. | `postularse`, `elegibilidad`, `revisarReglas`, `cancelarOtrasPostulaciones`, `misPostulaciones` |

## 2. Cómo se implementan las pruebas

- **Patrón Arrange - Act - Assert:** cada prueba marca las tres secciones con comentarios
  (`// Arrange`, `// Act`, `// Assert`).
- **Principios F.I.R.S.T:**
  - **Fast:** sin Spring ni base de datos; los 26 casos corren en menos de 1 segundo.
  - **Independent:** `@BeforeEach` crea datos nuevos para cada prueba.
  - **Repeatable:** el reloj es fijo (`Clock.fixed`), no depende de la fecha real.
  - **Self-validating:** cada prueba termina en `assert...`, sin revisión manual.
  - **Timely:** se escriben junto con la regla de negocio que prueban.
- **Trazabilidad:** cada prueba lleva su caso en `@DisplayName("CPxx | Tipo | Descripción")` y la
  etiqueta `@Tag("plan-pruebas")`. El informe del pipeline cruza ese ID con este plan.
- **Cobertura de casos especiales (punto 4):** cada componente tiene **al menos 2 pruebas
  negativas, 2 de borde y 1 de lógica de negocio**.

Ejecutar solo el plan: `cd backend && ./mvnw test -Dtest='*PlanTest'`

## 3. Casos de prueba

### Componente 1: `service/CalculadoraMatchScore`: fórmula del Match Score (RN-23, RN-20, RNF-09)

Clase de prueba: `CalculadoraMatchScorePlanTest`. Casos: 2 negativos, 3 de borde, 1 de negocio y 4 normales.

| Caso de prueba | Método | Descripción | Entrada de datos | Tipo de prueba | Resultados esperados | Resultados reales | Justificación |
|---|---|---|---|---|---|---|---|
| **CP01** | `calcular` | Todas las habilidades avaladas y misma senioridad | Avaladas={Java,SQL}; requeridas={Java,SQL}; Senior vs Senior | Normal | Match Score = 100.0; porcentaje = 1.0; sin faltantes | ✅ Coincide con lo esperado | Camino feliz de la formula RN-23 |
| **CP02** | `calcular (porcentaje de habilidades)` | Coincidencia parcial de habilidades | Avaladas={Java}; requeridas={Java,SQL}; Senior vs Senior | Normal | Porcentaje = 0.5; Match Score = 65.0; faltante = SQL | ✅ Coincide con lo esperado | Verifica los pesos 0.7 / 0.3 y que 65 coincide con el umbral |
| **CP03** | `calcular` | Vacante sin habilidades requeridas | Avaladas={}; requeridas={}; Senior vs Senior | Borde | Porcentaje = 1.0 (no divide entre cero); Match Score = 100.0 | ✅ Coincide con lo esperado | Lista vacía: evita la división por cero |
| **CP04** | `ajusteSenioridad` | Diferencia máxima de senioridad | Junior vs vacante Líder | Borde | Ajuste = 0.0 | ✅ Coincide con lo esperado | Último valor de la tabla de ajuste inferior |
| **CP05** | `ajusteSenioridad` | Empleado sin senioridad declarada | null vs Semi Senior | Borde | Ajuste = 0.6 (igual que Junior) | ✅ Coincide con lo esperado | Dato faltante: se toma el nivel más bajo |
| **CP06** | `calcular` | Habilidades avaladas nulas | Avaladas = null | Negativa | NullPointerException | ✅ Coincide con lo esperado | Una entrada inválida no debe producir un puntaje |
| **CP07** | `calcular` | Habilidades requeridas nulas | Requeridas = null | Negativa | NullPointerException | ✅ Coincide con lo esperado | Una entrada inválida no debe producir un puntaje |
| **CP08** | `calcular (RN-20)` | Habilidad declarada pero sin aval del manager | Avaladas={Java}; sin avalar={Docker}; requeridas={Java,Docker} | Negocio | Porcentaje = 0.5; Docker aparece en sinAvalar y faltantes | ✅ Coincide con lo esperado | RN-20: solo cuentan las habilidades avaladas |
| **CP09** | `Resultado.detalle` | Texto del cálculo para la bitácora | Resultado de 1 de 2 habilidades | Normal | El texto contiene "coinciden 1 de 2" y "= 65.0" | ✅ Coincide con lo esperado | RNF-06: la bitácora explica el cálculo |
| **CP10** | `ajusteSenioridad` | Empleado con senioridad superior a la pedida | Líder vs vacante Senior | Normal | Ajuste = 0.9 | ✅ Coincide con lo esperado | Superar la senioridad no penaliza fuerte |

### Componente 2: `model/Vacante`: ciclo de vida, mismo puesto y ranking (RN-02, RN-03, RN-10, RN-14, RN-27)

Clase de prueba: `VacantePlanTest`. Casos: 3 negativos, 2 de borde, 1 de negocio y 2 normales.

| Caso de prueba | Método | Descripción | Entrada de datos | Tipo de prueba | Resultados esperados | Resultados reales | Justificación |
|---|---|---|---|---|---|---|---|
| **CP11** | `activar` | Activar una vacante en borrador | Vacante BORRADOR; fecha = 18/11/2026 10:00 | Normal | Estado ACTIVA y fecha de activación registrada | ✅ Coincide con lo esperado | Ciclo de vida RN-02 |
| **CP12** | `cerrar` | Cerrar una vacante que no está activa | Vacante BORRADOR | Negativa | ReglaNegocioException; sigue en BORRADOR | ✅ Coincide con lo esperado | Transición de estado inválida |
| **CP13** | `programarActivacion` | Programar la activación sin fecha | fecha = null | Negativa | ReglaNegocioException; sigue en BORRADOR | ✅ Coincide con lo esperado | Evita una vacante pendiente que nunca se activa |
| **CP14** | `debeActivarse` | Ventana de 24 h exacta (RN-03) | Programada a T+24h; consulta en T+24h-1s y en T+24h | Borde | false un segundo antes; true justo al cumplirse | ✅ Coincide con lo esperado | Límite exacto de la ventana RN-03 |
| **CP15** | `calcularRanking` | Puntaje Final igual al umbral | Puntajes 65.0 y 64.9; umbral 65 | Borde | Solo entra la postulación de 65.0 | ✅ Coincide con lo esperado | Límite exacto del umbral RN-27 |
| **CP16** | `calcularRanking` | Orden del ranking | Puntajes 80, 72 (antes), 72 (después) y 95 rechazada | Negocio | Orden 80, 72 antes, 72 después; la rechazada no aparece | ✅ Coincide con lo esperado | RN-14: ranking inmutable y desempate por fecha |
| **CP17** | `esMismoPuestoQue` | Mismo puesto en otra rotación (RN-10) | Backend Senior vs "backend senior" del mismo proyecto; otro proyecto; null | Normal | true; false; false | ✅ Coincide con lo esperado | Define la "nueva rotación" de RN-10 |
| **CP18** | `descartar` | Descartar una vacante cerrada | Vacante CERRADA | Negativa | ReglaNegocioException; sigue CERRADA | ✅ Coincide con lo esperado | Una vacante cubierta no se puede descartar |

### Componente 3: `service/PostulacionService`: reglas de postulación (HU-008: RN-07, RN-09, RN-10, RN-28)

Clase de prueba: `PostulacionServicePlanTest`. Casos: 3 negativos, 2 de borde, 1 de negocio y 2 normales.

| Caso de prueba | Método | Descripción | Entrada de datos | Tipo de prueba | Resultados esperados | Resultados reales | Justificación |
|---|---|---|---|---|---|---|---|
| **CP19** | `postularse` | Postulación exitosa | Empleado 8 (12 meses en el rol); vacante 10 activa | Normal | Postulación ACTIVA guardada con su Puntaje Final | ✅ Coincide con lo esperado | Camino feliz de HU-008 |
| **CP20** | `postularse` | Empleado inexistente | idEmpleado = 999 | Negativa | RecursoNoEncontradoException; no se guarda nada | ✅ Coincide con lo esperado | Identificador inválido |
| **CP21** | `postularse` | Postulación repetida | Ya existe una postulación ACTIVA a la vacante 10 | Negativa | ReglaNegocioException "Ya tienes una postulación activa" | ✅ Coincide con lo esperado | No se duplica la postulación |
| **CP22** | `elegibilidad` | Un día antes de cumplir 6 meses en el rol (RN-07) | Inicio del rol = hoy - 6 meses + 1 día | Borde | No puede postularse; habilitado mañana | ✅ Coincide con lo esperado | Límite exacto de RN-07 |
| **CP23** | `elegibilidad` | 6 meses exactos después de un rechazo, con nueva rotación (RN-10) | Rechazo hace 6 meses en "Backend Senior"; nueva vacante del mismo puesto | Borde | Puede postularse | ✅ Coincide con lo esperado | Límite exacto de RN-10 |
| **CP24** | `cancelarOtrasPostulaciones` | Cancelación en cascada al aceptar (RN-09) | Postulaciones activas A (aceptada) y B | Negocio | B queda CANCELADA_POR_ROTACION; A sigue ACEPTADA | ✅ Coincide con lo esperado | RN-09: el empleado sale de los demás rankings |
| **CP25** | `elegibilidad` | Vacante cerrada | Vacante 10 en estado CERRADA | Negativa | No puede postularse: "La vacante no está recibiendo postulaciones." | ✅ Coincide con lo esperado | Solo se aceptan postulaciones a vacantes activas |
| **CP26** | `misPostulaciones` | Listado de mis postulaciones | Empleado 8 con una postulación (id 50) | Normal | 1 DTO con id 50 y vacante "Backend Senior" | ✅ Coincide con lo esperado | El empleado ve el estado de sus postulaciones |

## 4. Resumen

| Componente | Normal | Borde | Negativa | Negocio | Total |
|---|---|---|---|---|---|
| CalculadoraMatchScore | 4 | 3 | 2 | 1 | **10** |
| Vacante | 2 | 2 | 3 | 1 | **8** |
| PostulacionService | 2 | 2 | 3 | 1 | **8** |
| **Total** | | | | | **26** |

Además de estos 26 casos, la suite completa tiene **203 pruebas** (unitarias, de integración con
PostgreSQL, matriz de permisos y carga concurrente), con **93.6 % de cobertura de líneas** medida con JaCoCo.

## 5. Ejecución en el pipeline

`.github/workflows/ci.yml` ejecuta todas las pruebas en cada PR hacia `develop` o `main` y en cada
push a `develop`, `main`, `release/**` y `hotfix/**`. Después genera el informe
(`python3 scripts/reporte_pruebas.py`) con:

- casos ejecutados;
- casos exitosos (passed);
- casos fallidos (failed);
- mensajes de error.

El informe sale en HTML y en JSON, y se publica **siempre** como artefacto `informe-pruebas`.
