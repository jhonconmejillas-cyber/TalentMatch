# Base de datos — TalentMatch

PostgreSQL alojado en Supabase. El esquema se crea y actualiza con **Flyway**: al arrancar, la
API aplica en orden las migraciones que falten. No hace falta ejecutar SQL a mano.

| Carpeta | Contenido |
|---|---|
| `migrations/` | Migraciones versionadas del esquema (`V1__esquema_inicial.sql`, `V2__...`). |
| `seeds/` | Datos de prueba: catálogo de habilidades, empleados, managers, proyectos y vacantes de ejemplo. |

## Reglas para las migraciones

1. Un archivo fusionado en `develop` **nunca se edita**: los cambios van en una migración nueva
   con el siguiente número (`V2__agregar_x.sql`).
2. Los datos de prueba (`seeds/V1_1__datos_prueba.sql`) solo se cargan en desarrollo y pruebas.
   En producción se excluyen con la variable de entorno
   `FLYWAY_LOCATIONS=filesystem:database/migrations`.
3. Todos los usuarios de prueba tienen la contraseña `TalentMatch2026`
   (p. ej. `carlos.perez@talentmatch.com` es manager, `juan.ramirez@talentmatch.com` es empleado,
   `rrhh@talentmatch.com`, `lider@talentmatch.com`, `admin@talentmatch.com`).

## Entidades principales

| Tabla | Notas |
|---|---|
| `usuario` | Todos los roles (columna `rol`: `EMPLEADO`, `MANAGER`, `RRHH`, `LIDER_EJECUTIVO`, `ADMIN`). Contraseña con hash BCrypt. |
| `empleado` | Datos profesionales de empleados y managers. `fecha_inicio_rol` determina la antigüedad mínima de 6 meses (RN-07). |
| `habilidad`, `habilidad_empleado`, `wishlist` | Estado `AGREGADO` / `AVALADO` / `RECHAZADO`, quién avaló y cuándo (RN-19). |
| `proyecto`, `asignacion_proyecto` | Al liberarse una asignación se genera una vacante (RN-02). Máximo una asignación vigente por empleado. |
| `vacante`, `vacante_habilidad_critica`, `vacante_habilidad_deseable` | Estados: `BORRADOR`, `PENDIENTE_ACTIVACION`, `ACTIVA`, `CERRADA`, `DESCARTADA` (RN-03). |
| `postulacion` | Estados: `ACTIVA`, `ACEPTADA`, `RECHAZADA`, `CANCELADA_POR_ROTACION` (RN-09). |
| `bitacora_calculo` | **Solo lectura.** Un trigger rechaza `UPDATE` y `DELETE` (RNF-06). |
| `alerta` | Rechazos repetidos (RN-29) y vacantes sin match (RN-30). |
| `notificacion` | Mensajes in-app generados por el sistema (RNF-08). |

Los estados se guardan en mayúsculas porque así los escribe JPA a partir de los `enum` de Java, y
cada columna de estado tiene una restricción `CHECK` con los valores permitidos.

El diagrama E-R está en [`docs/diagramas`](../docs/diagramas/DC-TalentMatch-Diagrama-de-Clases.md).
