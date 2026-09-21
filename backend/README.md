# Backend — TalentMatch

API y logica del algoritmo de matching.

## Estructura

| Carpeta | Responsabilidad | Documentos |
|---|---|---|
| `src/api/routes/` | Endpoints HTTP. Cada endpoint verifica el rol del solicitante. | RNF-07 |
| `src/core/` | Configuracion (lee `conf/config.yaml`), seguridad, sesiones, bitacora. | RNF-06, RNF-07, RNF-12 |
| `src/models/` | Entidades: Empleado, Habilidad, Vacante, Postulacion, AsignacionProyecto, Bitacora. | — |
| `src/schemas/` | Contratos de entrada y salida. Aqui vive la garantia de que **ninguna respuesta expone la identidad del seleccionado**. | RN-18, CU_16 |
| `src/services/` | Reglas de negocio: matching, rotacion, notificaciones, alertas. | CU_11, CU_15…CU_19, CU_24 |
| `src/repositories/` | Acceso a datos. |  — |
| `tests/unit/` | Pruebas del calculo, de los filtros y de las reglas de plazos. | — |
| `tests/integration/` | Pruebas de flujo completo (postulacion -> decision -> rotacion). | — |

## Reglas que el backend debe garantizar

1. **Solo habilidades `avalado`** entran al Match Score (RN-20). Prueba unitaria obligatoria.
2. **Ningun dato personal** interviene en el calculo (RNF-09). Lista cerrada de campos.
3. **Todo calculo queda en bitacora** antes de devolverse. Si falla la bitacora, falla el calculo (RNF-06, CU_11).
4. **El ranking no se reordena** por ninguna via (RN-14).
5. **La respuesta de rechazo jamas incluye identificadores** del seleccionado (RN-18, CU_16).
