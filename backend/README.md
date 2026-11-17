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

## Como ejecutarlo

Requisitos: **JDK 21**. Maven no hace falta instalarlo: el proyecto trae el *wrapper* (`mvnw`).

```bash
# desde la raiz del repositorio
cp conf/config.example.yaml conf/config.yaml   # parametros locales (RNF-12)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`. Para comprobar que esta arriba:

```bash
curl http://localhost:8080/api/v1/health
# {"estado":"ok"}
```

Pruebas:

```bash
./mvnw test
```

Los parametros de `conf/config.yaml` se leen al arrancar (ver `application.properties`) y se
consultan desde cualquier Service a traves de `ParametrosService`. Si la carpeta `conf/` esta en
otra ruta, se indica con la variable de entorno `TALENTMATCH_CONF_DIR`.
