# Guía del backend

Paquete raíz: `co.edu.javeriana.talentmatch`, en `backend/src/main/java/`. Java 21, Spring Boot 4.1.1, Maven con wrapper (`./mvnw`, o `mvnw.cmd` en Windows).

## 1. Convenciones

- **Idioma:** clases, métodos, variables, mensajes de error y comentarios en **español**, igual que el dominio (`Vacante`, `postularse`, `ReglaNegocioException`). Los mensajes al usuario llevan tildes; los comentarios del código no las llevan.
- **Trazabilidad:** cada clase o método que implementa una regla la cita en su Javadoc (`RN-09`, `HU-008`, `CU_15`, `RNF-12`). Para encontrar dónde vive una regla: `grep -rn "RN-09" backend/src`.
- **Sin valores mágicos:** umbrales, pesos y plazos se leen de `conf/config.yaml` con `ParametrosService` o con las clases `*Properties`.
- **Tiempo:** nunca `LocalDateTime.now()`; siempre `LocalDateTime.now(reloj)` con el `Clock` inyectado.
- **Errores:** los servicios lanzan excepciones del dominio y `ManejadorErrores` las convierte en HTTP (ver [Arquitectura](Technical-Architecture#4-ciclo-de-vida-de-una-petición)).
- **DTO:** las entidades JPA nunca salen por la API. Cada respuesta es un `record` de `dto/` con un método estático `de(entidad)`.

## 2. Paquete `model/`: el dominio

Implementa el diagrama de clases (`docs/diagramas/DC-TalentMatch-Diagrama-de-Clases.md`).

| Clase | Qué representa | Métodos con reglas |
|---|---|---|
| `Usuario` (abstracta) | Persona con acceso. Herencia `SINGLE_TABLE` sobre la tabla `usuario`, discriminador `rol` | `mismoUsuarioQue`, `activar`/`desactivar` |
| `Empleado` | Empleado que se postula. Datos extra en la tabla secundaria `empleado` | `agregarHabilidad`, `habilidadesAvaladas` (RN-20), `asignacionActual`, `agregarAsignacion` (una sola vigente y del propio empleado), `fechaHabilitacionPorAntiguedad` (RN-07), `esManejadoPor` |
| `Manager` | Empleado que además publica vacantes y avala habilidades | `avalarHabilidad`/`rechazarHabilidad` (RN-19: solo su equipo), `aceptarCandidato`/`rechazarCandidato` (solo sus vacantes) |
| `AnalistaRRHH`, `LiderEjecutivo`, `Administrador` | Los otros roles | `Administrador` activa y desactiva usuarios |
| `Habilidad` | Catálogo de habilidades | `equals`/`hashCode` sin distinguir mayúsculas ("Docker" = "docker") |
| `HabilidadEmpleado` | Habilidad en el perfil, con estado `AGREGADO`, `AVALADO` o `RECHAZADO` | `avalar`, `rechazar`, `cuentaParaCalculo` |
| `Proyecto` | Proyecto de la empresa | `estaActivo` |
| `AsignacionProyecto` | Puesto de un empleado en un proyecto durante un periodo | `liberar(motivo, fecha)` (RN-02), `estaVigente` |
| `Vacante` | Puesto abierto. Estados: `BORRADOR` → `PENDIENTE_ACTIVACION` → `ACTIVA` → `CERRADA` o `DESCARTADA` | `activar`, `programarActivacion` (RN-03), `debeActivarse`, `cerrar`, `descartar`, `esMismoPuestoQue` (RN-10), `habilidadesRequeridas`, `calcularRanking` (RN-14, RN-27) |
| `Postulacion` | Petición de un empleado a una vacante. Estados: `ACTIVA`, `ACEPTADA`, `RECHAZADA`, `CANCELADA_POR_ROTACION` | `registrarPuntaje`, `esViable` (RN-27), `aceptar`, `rechazar`, `rechazarDirectamente` (RN-29), `cancelarPorRotacion` (RN-09), `registrarPosicion` |
| `BitacoraCalculo` | Registro inmutable de cada cálculo (`@Immutable`) | Solo se crea |
| `Alerta` | Alerta para RR.HH. (RN-29, RN-30) | `atender` |
| `Notificacion` | Notificación in-app | `marcarLeida` |
| `ReporteMensual` | Reporte de rechazos generado el día 1 (HU-002) | Solo se crea |
| `ReglaNegocioException` | Violación de una regla. Se responde 409 | — |
| Enums | `Rol`, `Senioridad` (`JUNIOR` < `SEMI_SENIOR` < `SENIOR` < `LIDER`), `EstadoVacante`, `EstadoPostulacion`, `EstadoHabilidad`, `OrigenVacante`, `MotivoFinAsignacion`, `TipoAlerta`, `TipoNotificacion`... | `Senioridad.diferenciaCon` |

## 3. Paquete `service/`: la lógica de negocio

| Servicio | Responsabilidad | Reglas e historias |
|---|---|---|
| `AuthService` | Registro (BCrypt, dominio corporativo, longitud mínima) y login | RF-01, RF-02, RNF-07 |
| `UsuarioService` | Administración de usuarios: listar, cambiar el rol, activar o desactivar | Rol Admin |
| `PerfilService` | Perfil profesional: senioridad, experiencia, área, fecha de inicio del rol y proyecto actual | HU del Sprint 1 (S1-09) |
| `HabilidadService` | Agregar habilidades al perfil y el aval o rechazo del manager actual | RN-19, RN-20, HU-007 |
| `WishlistService` | Habilidades que el empleado quiere aprender. Avisa cuando aparece una vacante que las pide | RF del perfil |
| `VacanteService` | Publicar, buscar y filtrar vacantes | S2-02 |
| `VacanteAutomaticaService` | Al liberarse una asignación crea la vacante del puesto (RN-02), la activa 24 h después si nace de una rotación (RN-03) y alerta las vacantes sin match (RN-05, RN-30) | HU-009 |
| `CalculadoraMatchScore` | **Fórmula pura** del Match Score. Solo recibe habilidades y senioridad, nunca el `Empleado` (neutralidad, RNF-09) | RN-23 |
| `MatchingService` | Calcula el Puntaje Final de un empleado para una vacante y **siempre** lo guarda en la bitácora | RN-13, RN-25, RNF-06 |
| `RecalculoService` | Recalcula los puntajes de las postulaciones activas cuando cambia algo que entra al cálculo (por ejemplo, un aval) | HU-005 |
| `PostulacionService` | Elegibilidad y postulación (RN-07, RN-10, RN-28) y cancelación en cascada (RN-09) | HU-008 |
| `RankingService` | Ranking de la vacante (solo viables, orden inmutable, alias `Empleado_011`, indicador de color), desglose con costo de rotación (RN-26) y corte del ciclo de 3 días (RN-12) | HU-001 |
| `DecisionService` | Aceptar o rechazar sin justificación. Al aceptar, en una transacción: cierra la vacante, rechaza a los demás, cancela otras postulaciones, mueve la asignación (RN-15), crea la vacante diferida del puesto anterior y notifica a RR.HH. (RN-16) | RN-13 a RN-17, HU-001 |
| `AlertaService` | Alerta a RR.HH. si un manager rechaza 2 o más candidatos viables (RN-29) | HU-004 |
| `ResultadoService` | Resultado anónimo para el rechazado: puntaje y habilidades del seleccionado, sin nombre (RN-17, RN-18) | HU-003 |
| `HistorialService` | Historial de proyectos y rotaciones con permiso por **relación**; las postulaciones solo las ve RR.HH. | HU-006, RF-30 |
| `AsignacionService` | RR.HH. consulta y libera asignaciones, lo que dispara RN-02 | CU_18 |
| `ReporteRechazosService` | Reporte mensual de patrones de rechazo por manager y su descarga | HU-002 |
| `ReporteEjecutivoService` | Top 10 por habilidades avaladas, rotaciones y antigüedad | RN-21, HU-010 |
| `NotificacionService` | Notificaciones in-app en la misma transacción que el evento | RNF-08 |
| `ParametrosService` | Punto único de lectura de los parámetros de negocio | RNF-12 |
| Excepciones | `AccesoDenegadoException` (403), `RecursoNoEncontradoException` (404), `DatosInvalidosException` (400), `CredencialesInvalidasException` (401) | — |

## 4. Paquete `controller/`

Un controlador por recurso: `AuthController`, `UsuarioController`, `PerfilController`, `HabilidadController`, `VacanteController`, `MatchingController`, `PostulacionController`, `RankingController`, `ResultadoController`, `AlertaController`, `GestionController`, `ReporteRechazosController`, `ReporteEjecutivoController` y `HealthController`. Más `ManejadorErrores` (`@RestControllerAdvice`). La lista completa de endpoints está en [Referencia de la API](API-Reference).

Reglas para los controladores:
- Anotan el rol con `@PreAuthorize` (a nivel de clase o de método).
- Obtienen el usuario autenticado del token (`Sesion`, en `security/`), **nunca** de un id enviado por el cliente.
- No tienen lógica: validan el formato y delegan al servicio.

## 5. Paquetes `security/` y `config/`

- `security/`: `SecurityConfig` (filtros, CORS, JWT), `TokenService` (emite y valida el JWT HS256), `RenovacionTokenFilter` (renueva el token en cada petición y bloquea cuentas desactivadas), `RespuestasSeguridad` (401 y 403 en JSON), `RegistroAccesosDenegados` (bitácora) y `Sesion` (usuario actual). Detalle en [Seguridad](Security).
- `config/`: las clases `*Properties` (una por sección de `config.yaml`), `CorsProperties`, `RelojConfig`, `TareasProgramadas` y `TiemposRespuestaFilter`.

## 6. Paquete `repository/`

Interfaces `JpaRepository` con *query methods* derivados del nombre, por ejemplo `existsByEmpleadoIdAndVacanteIdAndEstado` o `findByEmpleadoIdOrderByFechaCreacionDesc`. Usan `@Query` JPQL solo cuando hace falta. **No contienen lógica.**

## 7. Cómo agregar una funcionalidad (receta)

Ejemplo: "un empleado puede retirar su postulación".

1. **Regla y documentación:** si es una regla nueva, primero va en `docs/reglas-negocio` o se valida con el Product Owner.
2. **Modelo:** agrega el estado o método en la entidad (`Postulacion.retirar(ahora)`) con su protección (`if (!estaActiva()) throw new ReglaNegocioException(...)`).
3. **Migración:** si cambia la base, crea **una migración nueva** `database/migrations/V6__descripcion.sql`. Nunca edites una migración que ya se aplicó (ver [Modelo de datos](Database-Model#reglas-de-oro-de-flyway)).
4. **Servicio:** el caso de uso con `@Transactional`, que verifica permisos por relación (`AccesoDenegadoException`) y usa `Clock`.
5. **DTO:** el `record` de respuesta en `dto/`.
6. **Controlador:** el endpoint con `@PreAuthorize`.
7. **Permisos:** agrega la fila en `docs/seguridad/Matriz-de-Permisos.md` y en `MatrizPermisosTest`.
8. **Pruebas:** una unitaria del dominio o servicio (con Mockito y reloj fijo) y una de integración que herede de `ApiTest` (ver [Pruebas](Testing-and-Quality)).
9. **Frontend:** la acción en la página correspondiente, a través de `request()` de `api.js`.
10. **PR a `develop`:** el CI debe quedar en verde (ver [Flujo de desarrollo](Development-Workflow)).

## 8. Comandos útiles

```bash
cd backend
./mvnw spring-boot:run                 # arrancar la API en http://localhost:8080
./mvnw test                            # pruebas (requiere PostgreSQL y DB_URL/DB_USER/DB_PASSWORD)
./mvnw test -Dtest='*PlanTest'         # solo los 26 casos del plan de pruebas (no requieren BD)
./mvnw verify                          # pruebas + reporte de cobertura JaCoCo (target/site/jacoco)
./mvnw -q package -DskipTests          # solo empaquetar el .jar
```
