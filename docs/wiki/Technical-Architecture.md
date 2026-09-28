# Arquitectura Técnica

TalentMatch es una aplicación web organizada en **cuatro capas**. Cada capa tiene una sola
responsabilidad y **solo se comunica con la capa inmediatamente inferior**.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | HTML, CSS y JavaScript (sin framework), `fetch` para llamar a la API |
| Backend (Controller, Service, Repository) | Java 21 + Spring Boot |
| Persistencia | Spring Data JPA + migraciones con Flyway |
| Base de datos | PostgreSQL alojado en Supabase |
| Autenticación | Spring Security + BCrypt + token (RNF-07) |
| Pruebas | JUnit 5 |
| Despliegue | Cloudflare |

---

## Las 4 capas

```
+---------------------------------------------+
| 1. FRONTEND  (HTML + CSS + JavaScript)      |  Muestra y captura datos.
|    avalar.html · avalar.js · estilos.css    |  NO decide nada del negocio.
+----------------------+----------------------+
                       |  HTTP + JSON (API REST /api/v1)
+----------------------v----------------------+
| 2. CONTROLLER  (Java)                       |  Recibe la petición, valida el rol,
|    HabilidadController.java                 |  llama al servicio y responde JSON.
+---------------------------------------------+
| 3. SERVICE  (Java)  <- LÓGICA DE NEGOCIO    |  Aquí viven las Reglas de Negocio:
|    HabilidadService.java                    |  6 meses, solo "avalado", 65 %, etc.
+---------------------------------------------+
| 4. REPOSITORY  (Java)  <- PERSISTENCIA      |  Solo lee y guarda datos.
|    HabilidadEmpleadoRepository.java         |  NO contiene reglas de negocio.
+----------------------+----------------------+
                       |  SQL (JDBC)
+----------------------v----------------------+
|    PostgreSQL en Supabase                   |  Tablas del modelo Entidad-Relación.
+---------------------------------------------+
```

**Regla de oro:** el HTML nunca toca la base de datos y el Repository nunca decide si una acción
está permitida. Las reglas de negocio viven **únicamente** en la capa Service, por eso se pueden
probar con JUnit sin navegador ni base de datos.

### 1. Frontend — presentación

- Una página `.html` y un archivo `.js` por pantalla.
- `js/api.js` es la **única** puerta hacia el backend: agrega el token y maneja errores.
- Muestra mensajes en español (RNF-13) y respeta el máximo de 3 clics (RNF-03).
- No calcula puntajes ni valida reglas: solo muestra lo que responde la API.

### 2. Controller — entrada de la API

- Un controlador por recurso (`VacanteController`, `PostulacionController`, …).
- Valida el **rol** del solicitante (RNF-07) y el formato de los datos de entrada.
- No contiene reglas de negocio: delega todo al Service.

### 3. Service — lógica de negocio

- Implementa las Reglas de Negocio RN-01 … RN-30.
- Ejemplos: antigüedad mínima de 6 meses (RN-07), solo habilidades `avalado` en el cálculo
  (RN-20), umbral de 65 % (RN-27), ranking inmutable (RN-14), perfil anónimo del seleccionado
  (RN-18), bitácora obligatoria de cada cálculo (RNF-06).
- Lee los parámetros configurables desde `conf/config.yaml` (RNF-12).

### 4. Repository — persistencia

- Una interfaz JPA por entidad del diagrama de clases.
- Traduce objetos Java a filas de PostgreSQL y viceversa.
- Las tablas se crean con migraciones versionadas (`database/migrations/V1__...sql`).

---

## Ejemplo: un manager avala una habilidad (RN-19)

| Paso | Capa | Qué hace |
|---|---|---|
| 1 | Frontend `avalar.js` | `PATCH /api/v1/habilidades-empleado/{id}` con `{ "estado": "avalado" }` |
| 2 | `HabilidadController` | Verifica que el usuario tenga rol `MANAGER` y llama al servicio |
| 3 | `HabilidadService` | Verifica que sea el **manager actual** del empleado y que la habilidad no esté ya avalada; cambia el estado y registra fecha y quién avaló |
| 4 | `HabilidadEmpleadoRepository` | Guarda el cambio en la tabla `habilidad_empleado` |
| 5 | Frontend | Muestra la habilidad como avalada, o el mensaje de error que devolvió la API |

---

## Estructura de carpetas

```
backend/
├── pom.xml
└── src/
    ├── main/java/co/edu/javeriana/talentmatch/
    │   ├── controller/    un controlador por recurso REST
    │   ├── service/       reglas de negocio (RN)
    │   ├── repository/    interfaces JPA
    │   ├── model/         entidades del diagrama de clases
    │   ├── dto/           contratos JSON de entrada/salida (anonimato RN-18)
    │   ├── security/      login, token y permisos por rol
    │   └── config/        lectura de conf/config.yaml
    ├── main/resources/application.yml
    └── test/java/         pruebas JUnit por servicio

frontend/
├── index.html             login
├── pages/                 perfil.html, vacantes.html, ranking.html, avalar.html…
├── js/
│   ├── api.js             cliente HTTP único hacia el backend
│   ├── auth.js            sesión y redirección por rol
│   └── pages/             un .js por página
└── css/

database/migrations/       V1__esquema_inicial.sql, V2__…
conf/config.yaml           umbrales y plazos (RNF-12)
```

---

## API REST

Todas las rutas usan el prefijo `/api/v1` y validan el rol del solicitante.

| Recurso | Ruta | Rol | RF |
|---|---|---|---|
| Salud | `GET /health` | — | — |
| Sesión | `POST /auth/registro` · `POST /auth/login` · `GET /me` | Todos | RF-01, RF-02 |
| Usuarios | `GET /usuarios` · `PATCH /usuarios/{id}` | Administrador | RF-03 |
| Perfil | `GET` / `PUT /empleados/me/perfil` | Empleado | RF-04 |
| Catálogo | `GET /habilidades` | Todos | — |
| Mis habilidades | `GET` / `POST /empleados/me/habilidades` | Empleado | RF-06 |
| Aval | `GET /equipo/habilidades?estado=agregado` · `PATCH /habilidades-empleado/{id}` | Manager | RF-07 |
| Wishlist | `GET` / `POST` / `DELETE /empleados/me/wishlist` | Empleado | RF-09 |
| Vacantes | `GET /vacantes` (filtros) · `POST /vacantes` · `GET /vacantes/{id}` | Empleado / Manager | RF-11, RF-13 |
| Mi puntaje | `GET /vacantes/{id}/mi-puntaje` | Empleado | RF-15 … RF-17 |
| Postularse | `POST /vacantes/{id}/postulaciones` | Empleado | RF-19, RF-20 |
| Ranking | `GET /vacantes/{id}/ranking` | Manager | RF-18, RF-21, RF-22 |
| Decisión | `POST /postulaciones/{id}/aceptar` · `POST /postulaciones/{id}/rechazar` | Manager | RF-23 … RF-26 |
| Resultado anónimo | `GET /postulaciones/{id}/resultado` | Empleado | RF-27 |
| Historial propio | `GET /empleados/me/postulaciones` | Empleado | RF-31 |
| Historial de empleado | `GET /empleados/{id}/historial` | Manager / RR.HH. | RF-30 |
| Notificaciones | `GET /notificaciones` · `PATCH /notificaciones/{id}` | Todos | RF-10, RF-25 |
| Alertas | `GET /alertas` · `PATCH /alertas/{id}` | RR.HH. | RF-14, RF-32 |
| Carga masiva | `POST /cargas` (CSV) | RR.HH. | — |
| Configuración | `GET` / `PUT /configuracion` | RR.HH. | RF-33 |
| Reporte | `GET /reportes/top10` | Líder Ejecutivo | RF-29 |

**Tareas programadas** (no son endpoints; las ejecuta el backend con `@Scheduled`): activación de
vacantes 24 h después de una rotación (RN-03), alerta de vacante sin match a los 10 días (RN-05)
y ciclo de evaluación de 3 días (RN-12).

---

## Despliegue en Cloudflare

```
            Usuario (Chrome / Edge)
                     |
                     v
+--------------------------------------+
| Cloudflare Pages                     |   Frontend estático (HTML/CSS/JS)
+------------------+-------------------+
                   | HTTPS /api/v1
                   v
+--------------------------------------+
| Cloudflare Containers                |   Backend Spring Boot (imagen Docker)
+------------------+-------------------+
                   | JDBC (TLS)
                   v
+--------------------------------------+
| Supabase PostgreSQL                  |   Base de datos
+--------------------------------------+
```

| Componente | Servicio de Cloudflare | Notas |
|---|---|---|
| Frontend | **Cloudflare Pages** | Plan gratuito. Se publica automáticamente desde GitHub. |
| Backend | **Cloudflare Containers** | Ejecuta la imagen Docker de Spring Boot. Requiere el plan **Workers Paid**. |
| Base de datos | Supabase (fuera de Cloudflare) | El backend se conecta con la cadena del *connection pooler* de Supabase. |

### Ambientes

| Rama | Ambiente | Uso |
|---|---|---|
| `develop` | Preview | Probar la integración del sprint en curso |
| `main` | Producción | Versión entregada al cierre de cada sprint |

### Secretos

Las credenciales (cadena de conexión de Supabase, clave de firma del token) **nunca se suben al
repositorio**. Se guardan en:

- **Local:** archivo `.env` (incluido en `.gitignore`).
- **CI:** GitHub → Settings → Secrets and variables → Actions.
- **Producción:** variables secretas del proyecto en Cloudflare.
