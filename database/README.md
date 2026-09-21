# Base de datos — TalentMatch

| Carpeta | Contenido |
|---|---|
| `migrations/` | Migraciones versionadas del esquema. |
| `seeds/` | Datos de prueba: catalogo de habilidades, empleados y proyectos de ejemplo. |
| `schema.sql` | Esquema de referencia consolidado. |

## Entidades principales

| Entidad | Notas |
|---|---|
| `empleado` | Incluye `fecha_inicio_rol`, que determina la antiguedad minima de 6 meses (RN-07). |
| `habilidad_empleado` | Lleva el estado `agregado` / `avalado`, quien avalo y cuando (RN-19). |
| `proyecto`, `asignacion_proyecto` | Al liberarse una asignacion se genera una vacante (RN-02). |
| `vacante` | Estados: `borrador`, `pendiente_activacion`, `activa`, `cerrada`, `descartada` (RN-03). |
| `postulacion` | Estados: `activa`, `aceptada`, `rechazada`, `cancelada_por_rotacion` (RN-09). |
| `bitacora_calculo` | **Solo lectura.** Guarda el Match Score, el Puntaje Final, el umbral y la fecha de cada calculo (RNF-06). |
| `alerta` | Rechazos repetidos (RN-29) y vacantes sin match (RN-30). |
