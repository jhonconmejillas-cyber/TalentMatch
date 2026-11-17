# Backend — TalentMatch

API REST y logica del algoritmo de matching. **Java 21 + Spring Boot + Maven.**

## Las 4 capas

| Capa | Paquete (`co.edu.javeriana.talentmatch.*`) | Responsabilidad | Documentos |
|---|---|---|---|
| 1. Frontend | `frontend/` (fuera del backend) | Muestra y captura datos. No decide nada del negocio. | RNF-03, RNF-13 |
| 2. Controller | `controller/` | Recibe la peticion HTTP, valida el rol y el formato, llama al Service y responde JSON. | RNF-07 |
| 3. Service | `service/` | **Reglas de negocio** (RN-01 … RN-30): 6 meses, solo `avalado`, 65 %, ranking inmutable, bitacora. | CU_11, CU_15…CU_19, CU_24 |
| 4. Repository | `repository/` | Interfaces Spring Data JPA. Solo leen y guardan, no deciden. | — |

Paquetes de apoyo:

| Paquete | Contenido |
|---|---|
| `model/` | Entidades del [diagrama de clases](../docs/diagramas/DC-TalentMatch-Diagrama-de-Clases.md). |
| `dto/` | Contratos JSON de entrada y salida. Aqui vive la garantia de que **ninguna respuesta expone la identidad del seleccionado** (RN-18). |
| `security/` | Login, token y permisos por rol (RNF-07). |
| `config/` | Lectura de `conf/config.yaml` (RNF-12) y CORS. |

Las pruebas JUnit estan en `src/test/java/`, con la misma estructura de paquetes.

## Reglas que el backend debe garantizar

1. **Solo habilidades `avalado`** entran al Match Score (RN-20). Prueba unitaria obligatoria.
2. **Ningun dato personal** interviene en el calculo (RNF-09). Lista cerrada de campos.
3. **Todo calculo queda en bitacora** antes de devolverse. Si falla la bitacora, falla el calculo (RNF-06, CU_11).
4. **El ranking no se reordena** por ninguna via (RN-14).
5. **La respuesta de rechazo jamas incluye identificadores** del seleccionado (RN-18, CU_16).
