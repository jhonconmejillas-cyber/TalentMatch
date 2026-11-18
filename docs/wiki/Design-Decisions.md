# Decisiones e interpretaciones

Puntos que la especificación no definía con precisión y cómo se resolvieron en el código. Si el Product Owner decide otra cosa, aquí está **dónde cambiarlo**.

## Interpretaciones de reglas de negocio

| Tema | Qué dice la especificación | Cómo se implementó | Dónde cambiarlo |
|---|---|---|---|
| **"Jefe del área" (RN-05)** | Avisar al manager solicitante y al jefe del área cuando una vacante lleva 10 días sin match | El jefe del área es el **manager del manager solicitante**. Si no tiene, se avisa a RR.HH. | `VacanteAutomaticaService.revisarVacantesSinMatch` |
| **Ciclo de 3 días (RN-12)** | El manager evalúa las peticiones acumuladas cada 3 días | Los cortes caen cada 3 días desde la **activación** de la vacante. Solo se resuelven las postulaciones recibidas antes del **último corte**; las más nuevas esperan al siguiente y la API responde 409 con la fecha del próximo ciclo | `RankingService.corteCiclo`, `DecisionService`; parámetro `ciclo_evaluacion_dias` |
| **Identidad en el ranking (HU-001)** | Ejemplo con "Empleado_011" | El manager ve **alias** (`Empleado_` + id con 3 dígitos), no nombres. RR.HH. ve lo mismo en el ranking | `RankingService` (DTO del ranking) |
| **Críticas vs deseables (RN-23)** | La fórmula usa el "% de habilidades coincidentes" | Las críticas y las deseables **pesan igual** en el porcentaje | `CalculadoraMatchScore.calcular`, con `Vacante.habilidadesRequeridas()` |
| **Ajuste por senioridad (RN-23)** | Ajuste según la senioridad | Igual 1.0, superior 0.9, 1 abajo 0.6, 2 abajo 0.3, 3 abajo 0.0 | `conf/config.yaml` → `algoritmo.ajuste_senioridad_*` |
| **"No seleccionado" vs "rechazado" (RN-29)** | 2 rechazos de candidatos viables → alerta | Solo cuenta el rechazo **directo** del manager. Los que quedan sin seleccionar cuando se acepta a otro no cuentan (columna `rechazo_directo`, V2) | `AlertaService`, `Postulacion.rechazarDirectamente` |
| **Puntaje bajo (RN-28)** | Puede postularse con menos de 65 | No se bloquea; se crea la postulación y la respuesta trae `viable=false` y un aviso. No aparece en el ranking | `PostulacionService.postularse` |
| **Historial para managers (RNF-10)** | Solo el empleado y RR.HH. ven las postulaciones | El manager con relación (manager actual o vacante en común) ve proyectos y rotaciones, pero **no** las postulaciones | `HistorialService` |
| **Vacante sin proyecto** | — | `esMismoPuestoQue` compara título y área cuando no hay proyecto | `Vacante.esMismoPuestoQue` |

## Decisiones técnicas

| Decisión | Alternativa descartada | Razón |
|---|---|---|
| Monolito en un contenedor | Microservicios | Dominio pequeño y transacciones que cruzan entidades |
| Vercel + Render + Supabase | Cloudflare Pages + Containers | Containers exige plan de pago; todo lo elegido es gratis |
| Render (Docker) para el backend | Vercel | Vercel no ejecuta Java |
| Supabase con *Session pooler* | Conexión directa | La conexión directa es solo IPv6 y Render solo tiene IPv4 |
| Datos de prueba en producción | Base vacía | Es un MVP para demostrar; documentado cómo quitarlos |
| Contraseña `talento` mediante una migración nueva | Editar `V1_1` | Flyway valida el checksum de las migraciones ya aplicadas |
| JWT renovado en cada petición | Sesión en el servidor o *refresh token* | Expiración por inactividad sin estado en el servidor |
| `sessionStorage` en el frontend | `localStorage` | La sesión no sobrevive al cerrar la pestaña |
| Análisis de Sonar desde el CI | Análisis automático | El automático no mide la cobertura |
| CodeRabbit sin revisión incremental | Revisar cada push | Cupo de 10 revisiones por hora |
| Cobertura medida solo sobre el backend de producción | Cobertura de todo el repositorio | El frontend no tiene lógica de negocio ni pruebas unitarias; incluirlo distorsiona la métrica |
