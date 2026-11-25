# Mapa de reglas de negocio

Para cada regla de `docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md`: dónde está implementada y qué prueba la protege. Si cambias una regla, cambia su implementación **y** su prueba, y ejecuta la suite completa.

Para encontrar todas las referencias a una regla: `grep -rn "RN-10" backend/src frontend docs`.

| Regla | Resumen | Implementación principal | Pruebas |
|---|---|---|---|
| RN-01 | Modelo continuo: sin ventanas fijas de rotación | `VacanteService` (sin fechas límite) | `VacanteControllerTest` |
| RN-02 | La vacante se genera sola al liberarse una asignación | `AsignacionProyecto.liberar`, `VacanteAutomaticaService.liberarAsignacion` | `VacanteAutomaticaServiceTest`, `RankingControllerTest` (aceptar) |
| RN-03 | La vacante nacida de una rotación se activa 24 h después | `Vacante.programarActivacion`/`debeActivarse`/`activar`, cron `activarPendientes` | `VacanteAutomaticaServiceTest`, `VacantePlanTest` (CP13, CP14) |
| RN-04 | Cadena de vacantes: cada eslabón es independiente | `VacanteAutomaticaService` | `VacanteAutomaticaServiceTest` |
| RN-05 | Vacante sin match tras 10 días: avisar al manager y al jefe del área | `VacanteAutomaticaService.revisarVacantesSinMatch` (cron diario) | `VacanteAutomaticaServiceTest` |
| RN-06 | Postulaciones continuas, sin fecha límite | `PostulacionService` | `PostulacionControllerTest` |
| RN-07 | Mínimo 6 meses en el rol para postularse | `Empleado.fechaHabilitacionPorAntiguedad`, `PostulacionService.revisarReglas` | `PostulacionServiceTest`, `PostulacionServicePlanTest` (CP22) |
| RN-08 | Varias postulaciones activas a la vez | `PostulacionService`; índice `ux_postulacion_activa` (una por vacante) | `PostulacionControllerTest`, `PostulacionServicePlanTest` (CP21) |
| RN-09 | Al aceptar una, las demás se cancelan sin avisar a sus managers | `PostulacionService.cancelarOtrasPostulaciones`, llamado desde `DecisionService` | `PostulacionServicePlanTest` (CP24), `RankingControllerTest` |
| RN-10 | Repostular solo tras 6 meses **y** con una nueva rotación del mismo puesto | `Vacante.esMismoPuestoQue`, `PostulacionService.revisarReglas` | `PostulacionServiceTest`, `PostulacionServicePlanTest` (CP23), `VacantePlanTest` (CP17) |
| RN-11 | Se puede rotar entre áreas | `VacanteService` (sin filtro de área obligatorio) | `VacanteControllerTest` |
| RN-12 | El manager resuelve las peticiones en ciclos de 3 días | `RankingService.corteCiclo`, `DecisionService` (validación del corte) | `RankingControllerTest` (`lasPeticionesNuevasEsperanAlSiguienteCiclo`) |
| RN-13 | Se resuelve por mérito: Puntaje Final = Match Score | `MatchingService`, `DecisionService` | `MatchingServiceTest`, `RankingControllerTest` |
| RN-14 | Ranking inmutable | `Vacante.calcularRanking`; `RankingController` responde 409 a POST, PUT, PATCH y DELETE | `RankingControllerTest` (escenario 3), `VacantePlanTest` (CP16) |
| RN-15 | El cambio de asignación es automático | `DecisionService.moverAsignacion` | `RankingControllerTest` (aceptar) |
| RN-16 | Al aceptar, solo se notifica a RR.HH. | `DecisionService` + `NotificacionService` | `RankingControllerTest` |
| RN-17 | Sin feedback del manager; nada bloquea el cierre | `DecisionService` (rechazar sin justificación) | `RankingControllerTest` (`rechazarNoPideJustificacion`) |
| RN-18 | El rechazado ve el puntaje y las habilidades del seleccionado, sin identificarlo | `ResultadoService`, `ResultadoDtos` | `ResultadoControllerTest` (revisa el JSON) |
| RN-19 | Habilidades en estado `agregado` hasta que las avala el manager actual | `HabilidadEmpleado`, `Manager.avalarHabilidad`, `HabilidadService` | `HabilidadControllerTest` |
| RN-20 | Solo cuentan las habilidades avaladas | `Empleado.habilidadesAvaladas`, `CalculadoraMatchScore` | `CalculadoraMatchScoreTest`, `CalculadoraMatchScorePlanTest` (CP08) |
| RN-21 | Reporte top 10 para los líderes ejecutivos | `ReporteEjecutivoService` | `ReporteEjecutivoTest` |
| RN-22 | Cada rotación informa que mejora la posición en el reporte | `PostulacionDtos` / notificación `ROTACION_CONFIRMADA` | `RankingControllerTest` |
| RN-23 | Fórmula del Match Score | `CalculadoraMatchScore` + `AlgoritmoProperties` | `CalculadoraMatchScoreTest`, `CalculadoraMatchScorePlanTest` |
| RN-24 | *(Career Impact Score: eliminado del alcance del MVP, PEN-03)* | — | — |
| RN-25 | Puntaje Final = Match Score | `Postulacion.registrarPuntaje`, `MatchingService` | `MatchingServiceTest` |
| RN-26 | Costo de rotación (informativo, no altera el ranking) | `RankingService.costoRotacion`, `CostoRotacionProperties` | `RankingControllerTest` (escenario 2) |
| RN-27 | Viable si Puntaje Final ≥ 65 | `Postulacion.esViable`, `Vacante.calcularRanking` | `VacantePlanTest` (CP15) |
| RN-28 | Por debajo de 65 sí puede postularse, pero se le avisa | `PostulacionService.postularse`, `MatchingDtos` (aviso) | `PostulacionServiceTest` (escenario 2) |
| RN-29 | 2 o más rechazos directos de candidatos viables: alerta a RR.HH. | `AlertaService` (listener de `DecisionService`), columna `rechazo_directo` | `MeritoYAlertasTest`, `ReporteRechazosTest` |
| RN-30 | Vacante sin match tras 10 días: alerta | `VacanteAutomaticaService`, `TipoAlerta.VACANTE_SIN_MATCH` | `VacanteAutomaticaServiceTest` |

## Requisitos no funcionales relevantes

| Requisito | Implementación | Prueba |
|---|---|---|
| RNF-01: 50 usuarios concurrentes | Índices de V4, pool de conexiones | `CargaConcurrenteTest` |
| RNF-04 / HU-005: respuesta en menos de 3 s y ranking en menos de 2 s | Puntaje precalculado (`RecalculoService`), índices, `TiemposRespuestaFilter` | `RankingControllerTest` (20 candidatos), `CargaConcurrenteTest` |
| RNF-06: bitácora inmutable | `BitacoraCalculo` `@Immutable` + trigger | `BitacoraCalculoTest` |
| RNF-07: credenciales, HTTPS y sesión | BCrypt, JWT, `RenovacionTokenFilter` | `AuthControllerTest`, `ProteccionDatosTest` |
| RNF-09: neutralidad | `CalculadoraMatchScore.Entrada`, una lista cerrada de datos | `CalculadoraMatchScoreTest` (reflexión sobre el record) |
| RNF-10: privacidad del historial | `HistorialService` | `ProteccionDatosTest` |
| RNF-12: parámetros configurables | `conf/config.yaml` + `*Properties` | `ConfiguracionTest`, `ParametrosServiceTest` |
| HU-006: permisos | `@PreAuthorize`, `AccesoDenegadoException`, bitácora | `MatrizPermisosTest`, `ProteccionDatosTest` |
