# TalentMatch — Especificación de Casos de Uso

| Campo | Valor |
|---|---|
| **Documento** | Casos de Uso (CU) |
| **Versión** | 1.0 |
| **Fecha** | 2026-09-21 |
| **Sistema** | TalentMatch — Movilidad Interna de Ingenieros |
| **Total de casos de uso** | 25 (CU_01 – CU_25) |
| **Documentos fuente** | [Reglas de Negocio v2.0](../reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) · [Requerimientos v2.0](../requerimientos/RF-RNF-TalentMatch.md) · [Historias de Usuario v2.0](../historias-usuario/HU-TalentMatch.md) |

---

## 1. Introducción

Este documento especifica los **25 casos de uso** de TalentMatch, la plataforma de movilidad
interna que conecta ingenieros con vacantes dentro de su propia empresa mediante un algoritmo
**sin IA**, explicable y auditable.

Cada caso de uso se describe con el formato estándar del curso: identificador, nombre,
descripción, actores, secuencia normal en dos columnas (**Actor** / **Software**), excepciones,
casos de uso relacionados, precondición y postcondición.

Todos los casos de uso son **trazables** hacia atrás: cada uno realiza uno o más requerimientos
funcionales (RF), que a su vez se originan en una o más reglas de negocio (RN). La matriz
completa está en la [§4](#4-matriz-de-trazabilidad).

### 1.1 Actores del sistema

| Actor | Descripción |
|---|---|
| **Usuario** | Persona que aún no tiene cuenta en el sistema. Solo participa en CU_01. |
| **Empleado** | Ingeniero de la organización. Mantiene su perfil, define su meta de carrera, se postula a vacantes y consulta sus resultados. |
| **Manager / Líder Técnico** | Responsable de un equipo. Publica vacantes, avala habilidades de su gente, revisa el ranking y decide. |
| **Analista de RR.HH.** | Recibe la notificación de las rotaciones aceptadas, atiende alertas, carga datos maestros y configura el algoritmo. |
| **VP Engineering** | Define los requisitos técnicos de cada nivel de la escalera de carrera. |
| **Líder Ejecutivo** | Consume el reporte top 10 como insumo de ascensos. |
| **Administrador del Sistema** | Gestiona cuentas, roles y permisos. |
| **Sistema** | Actor interno: procesos automáticos (cálculos, generación de vacantes, notificaciones, alertas). |

### 1.2 Convenciones

- Los pasos del actor y del software se numeran de forma **continua** para que las excepciones
  puedan referenciar el paso exacto al que regresan.
- Las **excepciones** indican el paso de la secuencia normal donde se originan y el paso al que
  regresa el flujo.
- La columna **Software** de la sección de excepciones describe la respuesta del sistema.
- Los valores parametrizables (65 %, 0.4/0.6, 3 días, 6 meses, 24 horas, umbral de días sin
  match) se leen del archivo de configuración (RNF-12), no están fijos en el código.

### 1.3 Mapa de los casos de uso

```
ACCESO                   PERFIL Y HABILIDADES           VACANTES
CU_01 Registrarse        CU_03 Gestionar perfil         CU_09 Publicar vacante
CU_02 Iniciar sesion     CU_04 Definir meta carrera     CU_10 Buscar vacantes
CU_23 Gestionar usuarios CU_05 Agregar habilidad        CU_18 Generar vacante auto.
                         CU_06 Avalar habilidad         CU_19 Alertar vacante sin match
                         CU_07 Gestionar wishlist
                         CU_08 Notificar por wishlist

MATCHING                 POSTULACION Y DECISION         CONTROL Y REPORTES
CU_11 Calcular Puntaje   CU_12 Postularse               CU_20 Historial de proyectos
CU_13 Costo de rotacion  CU_14 Revisar ranking          CU_21 Historial propio
                         CU_15 Aceptar candidato        CU_22 Reporte ejecutivo top 10
                         CU_16 Rechazar candidato       CU_24 Alertar rechazos repetidos
                         CU_17 Cancelar postulaciones   CU_25 Configurar pesos y umbrales
```

---

## 2. Casos de uso

---

### CU_01 — Registrarse

| Identificador de Caso Uso | **CU_01** |
|:---|:---|
| **Nombre** | Registrarse |
| **Descripción** | Este caso de uso permite al usuario crear una cuenta en el sistema. |
| **Actores** | Usuario |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El usuario selecciona la opción "Registrarse". | |
| | 2. El sistema solicita al usuario los siguientes datos: nombre completo, correo electrónico corporativo y contraseña. |
| 3. El usuario ingresa los datos requeridos. | |
| | 4. Valida la información ingresada: formato del correo, dominio corporativo, que el correo no esté ya registrado y que la contraseña cumpla la longitud mínima. |
| | 5. El sistema almacena los datos proporcionados —la contraseña con hash, nunca en texto plano— y muestra un mensaje de confirmación al usuario. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. Error al ingresar los datos. | a. Muestra un mensaje de error en caso de que algún dato sea incorrecto, indicando el campo afectado.<br>b. Regresar al paso 3. |
| 4. El correo electrónico ya está registrado. | a. Informa que la cuenta ya existe y ofrece iniciar sesión o recuperar la contraseña.<br>b. Regresar al paso 3. |
| **CU relacionados** | **CU_02**, CU_03, CU_23 |
| **Precondición** | El usuario no está registrado en el sistema. |
| **Post condición** | Nuevo usuario registrado en la base de datos del sistema, con rol por defecto "Empleado" pendiente de confirmación por el Administrador. |
| **Requerimientos** | RF-01 · RNF-07, RNF-13 |

---

### CU_02 — Iniciar sesión

| Identificador de Caso Uso | **CU_02** |
|:---|:---|
| **Nombre** | Iniciar sesión |
| **Descripción** | Este caso de uso permite a un usuario registrado autenticarse y acceder al panel correspondiente a su rol. |
| **Actores** | Empleado, Manager / Líder Técnico, Analista de RR.HH., VP Engineering, Líder Ejecutivo, Administrador del Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El usuario selecciona la opción "Iniciar sesión". | |
| | 2. El sistema solicita correo electrónico y contraseña. |
| 3. El usuario ingresa sus credenciales. | |
| | 4. Valida las credenciales comparando el hash almacenado. |
| | 5. El sistema crea la sesión, identifica el rol del usuario y carga el panel correspondiente. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. Credenciales incorrectas. | a. Muestra el mensaje "Correo o contraseña incorrectos", sin revelar cuál de los dos falló.<br>b. Regresar al paso 3. |
| 4. Cuenta desactivada. | a. Informa que la cuenta está desactivada e indica contactar al Administrador del Sistema.<br>b. El caso de uso termina. |
| 5. Sesión previa expirada por inactividad. | a. Cierra la sesión anterior y exige reautenticación antes de mostrar cualquier dato.<br>b. Regresar al paso 3. |
| **CU relacionados** | CU_01, CU_23 |
| **Precondición** | El usuario está registrado y su cuenta está activa. |
| **Post condición** | Sesión abierta con los permisos de su rol; toda acción posterior queda asociada a esa sesión. |
| **Requerimientos** | RF-02 · RNF-07 |

---

### CU_03 — Gestionar perfil profesional

| Identificador de Caso Uso | **CU_03** |
|:---|:---|
| **Nombre** | Gestionar perfil profesional |
| **Descripción** | Este caso de uso permite al empleado registrar y actualizar los datos profesionales que alimentan el cálculo del Match Score. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Mi perfil". | |
| | 2. El sistema muestra sus datos actuales: senioridad, años de experiencia, área, proyecto actual y fecha de inicio en el rol. |
| 3. El empleado modifica los datos que desea actualizar. | |
| 4. El empleado confirma los cambios. | |
| | 5. Valida que los años de experiencia sean coherentes con la senioridad y que las fechas no sean futuras. |
| | 6. El sistema guarda los cambios y muestra un mensaje de confirmación. |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 5. Datos inconsistentes o fuera de rango. | a. Muestra un mensaje indicando el campo y el rango válido.<br>b. Regresar al paso 3. |
| 5. El empleado intenta modificar su fecha de inicio en el rol. | a. Rechaza el cambio: esa fecha la establece el sistema al ejecutarse una rotación (CU_15) y determina la antigüedad exigida por RN-07.<br>b. Regresar al paso 3. |
| **CU relacionados** | CU_04, CU_05, **CU_11** |
| **Precondición** | El empleado tiene sesión iniciada (CU_02). |
| **Post condición** | El perfil queda actualizado y los cálculos de Puntaje Final posteriores usan los nuevos datos. |
| **Requerimientos** | RF-04 · RN-07, RN-23 |

---

### CU_04 — Definir meta de carrera

| Identificador de Caso Uso | **CU_04** |
|:---|:---|
| **Nombre** | Definir meta de carrera |
| **Descripción** | Este caso de uso permite al empleado elegir su meta de carrera desde la escalera predefinida, dato que determina el Career Impact Score. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Mi meta de carrera". | |
| | 2. El sistema muestra la escalera de carrera disponible con los niveles definidos por VP Engineering y RR.HH. (CU_25). |
| 3. El empleado selecciona el nivel que quiere alcanzar (p. ej. "Staff Engineer"). | |
| | 4. Muestra los requisitos técnicos de ese nivel y cuáles cubre ya con sus habilidades avaladas. |
| 5. El empleado confirma la meta. | |
| | 6. El sistema guarda la meta y recalcula el Career Impact Score de todas sus postulaciones activas. |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. La escalera de carrera no está configurada. | a. Informa que la escalera aún no ha sido definida e indica contactar a RR.HH.<br>b. El caso de uso termina. |
| 5. El empleado selecciona un nivel inferior al actual. | a. Solicita confirmación explícita advirtiendo que reducirá su Career Impact Score en las vacantes de mayor nivel.<br>b. Regresar al paso 3. |
| **CU relacionados** | CU_03, **CU_11**, CU_25 |
| **Precondición** | El empleado tiene sesión iniciada y existe al menos una escalera de carrera configurada. |
| **Post condición** | La meta de carrera queda asociada al empleado y el Career Impact Score de sus postulaciones se actualiza. |
| **Requerimientos** | RF-05 · RN-24 · PEN-03 |

---

### CU_05 — Agregar habilidad al perfil

| Identificador de Caso Uso | **CU_05** |
|:---|:---|
| **Nombre** | Agregar habilidad al perfil |
| **Descripción** | Este caso de uso permite al empleado añadir una habilidad a su perfil. La habilidad queda siempre en estado "agregado" hasta que su manager la avale. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Agregar habilidad". | |
| | 2. El sistema solicita la habilidad (del catálogo) y el nivel declarado. |
| 3. El empleado selecciona la habilidad y el nivel. | |
| | 4. Valida que la habilidad exista en el catálogo y que el empleado no la tenga ya registrada. |
| | 5. El sistema guarda la habilidad en estado **"agregado"**, la envía a la bandeja de aval de su manager actual y advierte al empleado que **no contará** en su Puntaje Final hasta ser avalada. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. La habilidad ya está registrada en el perfil. | a. Informa que ya existe y muestra su estado actual.<br>b. Regresar al paso 3. |
| 4. La habilidad no existe en el catálogo. | a. Ofrece solicitar su alta al Analista de RR.HH.<br>b. Regresar al paso 3. |
| 5. El empleado no tiene un manager actual asignado. | a. Guarda la habilidad en estado "agregado" y encola la solicitud de aval hasta que se le asigne un manager.<br>b. El caso de uso termina. |
| **CU relacionados** | **CU_06**, CU_07, CU_11 |
| **Precondición** | El empleado tiene sesión iniciada. |
| **Post condición** | Habilidad registrada en estado "agregado", pendiente de aval, y excluida del cálculo del Puntaje Final. |
| **Requerimientos** | RF-06 · RN-19, RN-20 |

---

### CU_06 — Avalar habilidad del equipo

| Identificador de Caso Uso | **CU_06** |
|:---|:---|
| **Nombre** | Avalar habilidad del equipo |
| **Descripción** | Este caso de uso permite al manager actual revisar las habilidades autoreportadas por los empleados de su equipo y cambiarlas a estado "avalado" o rechazarlas. |
| **Actores** | Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona la opción "Habilidades por avalar". | |
| | 2. El sistema muestra la lista de habilidades en estado "agregado" de los empleados de su equipo, con el empleado, la habilidad, el nivel declarado y la fecha. |
| 3. El manager selecciona una habilidad y elige "Avalar" o "Rechazar". | |
| | 4. Valida que el manager sea efectivamente el manager actual del empleado. |
| | 5. El sistema actualiza el estado a **"avalado"** o lo mantiene en "agregado" si fue rechazado, registra quién y cuándo tomó la decisión, y notifica al empleado. |
| | 6. Si la habilidad fue avalada, recalcula el Match Score de las postulaciones activas de ese empleado. |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. El manager no es el manager actual del empleado. | a. Deniega la acción, muestra "No tiene permisos sobre este empleado" y registra el intento en la bitácora.<br>b. Regresar al paso 3. |
| 2. No hay habilidades pendientes. | a. Muestra "No hay habilidades pendientes de aval".<br>b. El caso de uso termina. |
| **CU relacionados** | **CU_05**, **CU_11**, CU_22 |
| **Precondición** | El manager tiene sesión iniciada y existe al menos una habilidad en estado "agregado" en su equipo. |
| **Post condición** | La habilidad queda avalada y pasa a contar en el Puntaje Final, o permanece como autoreportada sin efecto en el cálculo. |
| **Requerimientos** | RF-07, RF-08 · RN-19, RN-20 |

---

### CU_07 — Gestionar lista de deseos (wishlist)

| Identificador de Caso Uso | **CU_07** |
|:---|:---|
| **Nombre** | Gestionar lista de deseos (wishlist) |
| **Descripción** | Este caso de uso permite al empleado registrar las habilidades que desea aprender, para que el sistema le avise cuando aparezca una vacante que las ofrezca. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Mis deseos". | |
| | 2. El sistema muestra su wishlist actual y el catálogo de habilidades disponibles. |
| 3. El empleado agrega o elimina habilidades de su wishlist. | |
| | 4. Valida que la habilidad exista en el catálogo y que no esté ya avalada en su perfil. |
| | 5. El sistema guarda la wishlist y activa la vigilancia de vacantes para esas habilidades. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. La habilidad ya está avalada en el perfil del empleado. | a. Informa que ya la domina y sugiere elegir otra.<br>b. Regresar al paso 3. |
| 4. Se supera el máximo de habilidades en la wishlist. | a. Informa el límite configurado y solicita eliminar alguna antes de agregar otra.<br>b. Regresar al paso 3. |
| **CU relacionados** | **CU_08**, CU_05, CU_10 |
| **Precondición** | El empleado tiene sesión iniciada. |
| **Post condición** | Wishlist actualizada y vigilancia de vacantes activa para las habilidades seleccionadas. |
| **Requerimientos** | RF-09 |

---

### CU_08 — Notificar vacante por coincidencia con la wishlist

| Identificador de Caso Uso | **CU_08** |
|:---|:---|
| **Nombre** | Notificar vacante por coincidencia con la wishlist |
| **Descripción** | Este caso de uso describe cómo el sistema avisa proactivamente al empleado cuando se activa una vacante que cubre habilidades de su lista de deseos. |
| **Actores** | Sistema, Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema detecta que una vacante pasó a estado "activa". |
| | 2. Compara las habilidades críticas y deseables de la vacante contra las wishlist de todos los empleados. |
| | 3. Para cada coincidencia, calcula el Puntaje Final del empleado frente a esa vacante (CU_11). |
| | 4. Genera y envía la notificación con el nombre de la vacante, las habilidades coincidentes y el Puntaje Final estimado, en menos de 5 minutos. |
| 5. El empleado abre la notificación. | |
| | 6. El sistema muestra el detalle de la vacante y ofrece la opción de postularse (CU_12). |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 3. El empleado no cumple la antigüedad mínima de 6 meses en su rol. | a. Envía igualmente la notificación, pero indicando la fecha a partir de la cual podrá postularse.<br>b. Continuar en el paso 4. |
| 3. El empleado tiene un bloqueo de 6 meses vigente sobre ese puesto. | a. No envía la notificación para esa vacante.<br>b. El caso de uso termina para ese empleado. |
| 4. Falla el envío de la notificación. | a. Reintenta hasta 3 veces y, si persiste, deja la notificación disponible dentro de la aplicación y registra el error.<br>b. Continuar en el paso 5. |
| **CU relacionados** | CU_07, **CU_18**, CU_11, CU_12 |
| **Precondición** | Existe al menos una vacante recién activada y empleados con wishlist configurada. |
| **Post condición** | Los empleados con coincidencias reciben la notificación y pueden postularse directamente desde ella. |
| **Requerimientos** | RF-10 · RNF-08 · RN-01, RN-07, RN-10 |

---

### CU_09 — Publicar vacante interna

| Identificador de Caso Uso | **CU_09** |
|:---|:---|
| **Nombre** | Publicar vacante interna |
| **Descripción** | Este caso de uso permite al manager publicar manualmente una vacante para su equipo, indicando las habilidades y el nivel requeridos. |
| **Actores** | Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona la opción "Publicar vacante". | |
| | 2. El sistema solicita: título del puesto, proyecto, área, nivel de senioridad, habilidades críticas y habilidades deseables. |
| 3. El manager ingresa los datos requeridos. | |
| | 4. Valida que exista al menos una habilidad crítica, que todas pertenezcan al catálogo y que el proyecto esté activo. |
| | 5. El sistema crea la vacante en estado "activa", la publica para todas las áreas y dispara la comparación con las wishlist (CU_08). |
| | 6. Muestra un mensaje de confirmación con el identificador de la vacante. |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. No se indicó ninguna habilidad crítica. | a. Muestra "Debe indicar al menos una habilidad crítica: sin ella no es posible calcular el Match Score".<br>b. Regresar al paso 3. |
| 4. Alguna habilidad no existe en el catálogo. | a. Señala la habilidad no reconocida y ofrece solicitar su alta a RR.HH.<br>b. Regresar al paso 3. |
| 4. El proyecto indicado está cerrado. | a. Informa que no se pueden publicar vacantes sobre proyectos cerrados.<br>b. Regresar al paso 3. |
| **CU relacionados** | **CU_18**, CU_08, CU_10, CU_14, CU_19 |
| **Precondición** | El manager tiene sesión iniciada y es responsable de al menos un proyecto activo. |
| **Post condición** | Vacante creada en estado "activa", visible para todos los empleados de la organización, incluidos los de otras áreas. |
| **Requerimientos** | RF-11 · RN-01, RN-11 |

---

### CU_10 — Buscar y filtrar vacantes internas

| Identificador de Caso Uso | **CU_10** |
|:---|:---|
| **Nombre** | Buscar y filtrar vacantes internas |
| **Descripción** | Este caso de uso permite al empleado explorar las vacantes activas de toda la organización y filtrarlas según sus criterios. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Explorar vacantes". | |
| | 2. El sistema muestra todas las vacantes en estado "activa", de cualquier área, con su Puntaje Final ya calculado para ese empleado (CU_11). |
| 3. El empleado aplica filtros por título, habilidad, área o senioridad. | |
| | 4. Valida los filtros y recupera las vacantes que los cumplen. |
| | 5. El sistema muestra el resultado ordenado por Puntaje Final descendente, indicando en cada una las habilidades coincidentes. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. Ningún resultado coincide con los filtros. | a. Muestra "No hay vacantes que coincidan" y sugiere ampliar los criterios o agregar habilidades a su wishlist (CU_07).<br>b. Regresar al paso 3. |
| 2. No hay vacantes activas en la organización. | a. Muestra "No hay vacantes activas en este momento" e invita a configurar su wishlist para recibir avisos.<br>b. El caso de uso termina. |
| 2. Existen vacantes en estado "pendiente de activación". | a. No las incluye en el listado: solo se muestran cuando cumplen las 24 horas (RN-03).<br>b. Continuar en el paso 3. |
| **CU relacionados** | CU_07, CU_11, **CU_12**, CU_18 |
| **Precondición** | El empleado tiene sesión iniciada. |
| **Post condición** | El empleado conoce las vacantes disponibles y su compatibilidad con cada una. |
| **Requerimientos** | RF-13 · RNF-03, RNF-04 · RN-03, RN-11 |

---

### CU_11 — Calcular Puntaje Final

| Identificador de Caso Uso | **CU_11** |
|:---|:---|
| **Nombre** | Calcular Puntaje Final |
| **Descripción** | Este caso de uso describe el cálculo de las métricas del algoritmo de matching —Match Score, Career Impact Score y Puntaje Final— para un par empleado–vacante. Es el núcleo del sistema. |
| **Actores** | Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema recibe la solicitud de cálculo para un empleado y una vacante (originada en CU_08, CU_10, CU_12 o CU_14). |
| | 2. Recupera las habilidades del empleado **en estado "avalado" únicamente**, su senioridad y su meta de carrera. |
| | 3. Calcula el Match Score: `(% de habilidades avaladas coincidentes × 0.7) + (ajuste por senioridad × 0.3)`. |
| | 4. Calcula el Career Impact Score: % de los requisitos de la meta de carrera del empleado que cubre la vacante. |
| | 5. Calcula el Puntaje Final: `Match Score × 0.4 + Career Impact Score × 0.6`, con los pesos leídos de la configuración. |
| | 6. Registra en la bitácora: empleado, vacante, las tres métricas, los pesos, el umbral aplicado y la fecha y hora. |
| | 7. Devuelve el resultado con el desglose explicable de cada métrica. |
| | 8. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. El empleado no tiene ninguna habilidad avalada. | a. Calcula Match Score = 0 e informa al empleado que debe solicitar el aval de sus habilidades a su manager (CU_06).<br>b. Continuar en el paso 4. |
| 4. El empleado no ha definido meta de carrera. | a. Calcula Career Impact Score = 0, advierte que está perdiendo el 60 % del puntaje y sugiere definirla (CU_04).<br>b. Continuar en el paso 5. |
| 5. Los pesos configurados no suman 1.0. | a. Aborta el cálculo, usa los valores por defecto (0.4 / 0.6), registra el error y alerta al Analista de RR.HH.<br>b. Continuar en el paso 6. |
| 6. Falla el registro en la bitácora. | a. Aborta la operación y no devuelve resultado: ningún puntaje puede usarse si no queda trazado (RNF-06).<br>b. El caso de uso termina con error. |
| **CU relacionados** | CU_03, CU_04, **CU_06**, CU_08, CU_10, **CU_12**, **CU_14**, CU_25 |
| **Precondición** | Existen el empleado y la vacante, y los pesos y el umbral están configurados. |
| **Post condición** | Las tres métricas quedan calculadas, devueltas con su desglose y registradas de forma inmutable en la bitácora. |
| **Requerimientos** | RF-15, RF-16, RF-08 · RNF-06, RNF-09, RNF-12 · RN-13, RN-20, RN-23, RN-24, RN-25 |

---

### CU_12 — Postularse a una vacante

| Identificador de Caso Uso | **CU_12** |
|:---|:---|
| **Nombre** | Postularse a una vacante |
| **Descripción** | Este caso de uso permite al empleado registrar su postulación a una vacante interna, validando las reglas de antigüedad y de bloqueo por rechazo previo. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona una vacante y presiona "Postularme". | |
| | 2. Valida que el empleado lleve **mínimo 6 meses** en su rol actual. |
| | 3. Valida que no exista un bloqueo vigente por rechazo previo en ese puesto. |
| | 4. Calcula el Puntaje Final del empleado para esa vacante (CU_11). |
| | 5. Muestra al empleado su Puntaje Final con el desglose de las tres métricas y solicita confirmación. |
| 6. El empleado confirma la postulación. | |
| | 7. El sistema registra la postulación en estado "activa" y la agrega al lote de peticiones que el manager resolverá en su próximo ciclo de 3 días. |
| | 8. Muestra un mensaje de confirmación indicando la fecha estimada de resolución. |
| | 9. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. El empleado lleva menos de 6 meses en su rol actual. | a. Bloquea la postulación e informa: "Podrás postularte a partir del [fecha]: se requieren 6 meses en el rol actual".<br>b. El caso de uso termina. |
| 3. Existe un bloqueo vigente por rechazo previo en ese puesto. | a. Bloquea la postulación e informa que debe esperar a que se cumplan los 6 meses **y** se abra una nueva rotación en ese puesto.<br>b. El caso de uso termina. |
| 5. El Puntaje Final es inferior al 65 %. | a. Advierte al empleado que sus posibilidades son bajas y que **no aparecerá en el ranking del manager**, pero **permite** continuar con la postulación.<br>b. Continuar en el paso 6. |
| 7. El empleado ya tiene una postulación activa a esa misma vacante. | a. Informa que ya está postulado y muestra el estado actual de esa postulación.<br>b. El caso de uso termina. |
| 7. La vacante fue cerrada mientras el empleado decidía. | a. Informa que la vacante ya no está disponible y regresa al listado (CU_10).<br>b. El caso de uso termina. |
| **CU relacionados** | CU_10, **CU_11**, **CU_14**, CU_16, **CU_17**, CU_21 |
| **Precondición** | El empleado tiene sesión iniciada, la vacante está en estado "activa" y el empleado no tiene una postulación activa a ella. |
| **Post condición** | Postulación registrada en estado "activa". El empleado puede mantener varias postulaciones activas simultáneamente. |
| **Requerimientos** | RF-19, RF-20, RF-17 · RN-06, RN-07, RN-08, RN-10, RN-28 |

---

### CU_13 — Consultar costo de rotación

| Identificador de Caso Uso | **CU_13** |
|:---|:---|
| **Nombre** | Consultar costo de rotación |
| **Descripción** | Este caso de uso muestra al manager el argumento económico de aceptar a un candidato interno: el costo estimado de perder al empleado frente al costo de moverlo internamente. |
| **Actores** | Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona un candidato del ranking y abre "Costo de rotación". | |
| | 2. Recupera los parámetros de costo configurados: recruiting, ramp-up, pérdida de conocimiento, capacitación, transición y baja temporal. |
| | 3. Calcula el costo estimado de que el empleado renuncie a la empresa. |
| | 4. Calcula el costo estimado de moverlo internamente. |
| | 5. El sistema muestra ambos valores y el ahorro estimado, con el detalle de cada componente. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. Los parámetros de costo no están configurados. | a. Muestra "Métrica no disponible: los parámetros de costo no han sido cargados por RR.HH." y oculta el indicador.<br>b. El caso de uso termina. |
| 5. La métrica está fuera del alcance confirmado (PEN-02). | a. Muestra el valor marcado como **informativo** y advierte explícitamente que **no influye en el Puntaje Final ni en el orden del ranking**.<br>b. Continuar en el paso 6. |
| **CU relacionados** | **CU_14**, CU_15, CU_25 |
| **Precondición** | El manager tiene sesión iniciada y está revisando el ranking de una vacante suya. |
| **Post condición** | El manager dispone del argumento económico. El valor **no altera** el ranking, que sigue siendo estrictamente por Puntaje Final. |
| **Requerimientos** | RF-18 · RN-26 · PEN-02 |

---

### CU_14 — Revisar ranking de candidatos

| Identificador de Caso Uso | **CU_14** |
|:---|:---|
| **Nombre** | Revisar ranking de candidatos |
| **Descripción** | Este caso de uso permite al manager revisar, cada 3 días, el ranking inmutable de candidatos viables que se postularon a su vacante. |
| **Actores** | Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona una de sus vacantes activas. | |
| | 2. Recupera las postulaciones acumuladas del ciclo de 3 días. |
| | 3. Calcula o recupera el Puntaje Final de cada candidato (CU_11). |
| | 4. Filtra y conserva únicamente los candidatos con Puntaje Final **≥ 65 %**. |
| | 5. El sistema muestra el ranking ordenado de mayor a menor Puntaje Final, con indicador de color, en menos de 2 segundos. |
| 6. El manager selecciona un candidato para ver su desglose. | |
| | 7. Muestra Match Score, Career Impact Score, habilidades avaladas coincidentes y costo de rotación (CU_13), sin perder el contexto del ranking. |
| | 8. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. Ningún candidato alcanza el 65 %. | a. Muestra "No hay candidatos viables en este ciclo" e informa cuántas postulaciones quedaron por debajo del umbral, sin revelar su identidad.<br>b. El caso de uso termina. |
| 5. El manager intenta reordenar, arrastrar o fijar un candidato. | a. La interfaz no lo permite y la API rechaza la petición: el ranking es inmutable (RN-14). Registra el intento en la bitácora.<br>b. Regresar al paso 5. |
| 2. El ciclo de 3 días aún no se ha cumplido. | a. Muestra las postulaciones recibidas hasta el momento marcadas como "en acumulación" e indica la fecha de resolución.<br>b. Continuar en el paso 3. |
| **CU relacionados** | CU_09, **CU_11**, CU_12, CU_13, **CU_15**, **CU_16** |
| **Precondición** | El manager tiene sesión iniciada, la vacante es suya y tiene al menos una postulación. |
| **Post condición** | El manager conoce el ranking ordenado por mérito y puede aceptar o rechazar candidatos. |
| **Requerimientos** | RF-21, RF-22 · RNF-03, RNF-04, RNF-09 · RN-12, RN-13, RN-14, RN-27 |

---

### CU_15 — Aceptar candidato y ejecutar rotación

| Identificador de Caso Uso | **CU_15** |
|:---|:---|
| **Nombre** | Aceptar candidato y ejecutar rotación |
| **Descripción** | Este caso de uso permite al manager aceptar a un candidato del ranking, lo que desencadena el cambio automático de asignación, la notificación a RR.HH. y la apertura de la vacante del puesto liberado. |
| **Actores** | Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona un candidato del ranking y presiona "Aceptar". | |
| | 2. El sistema solicita confirmación mostrando el Puntaje Final del candidato. |
| 3. El manager confirma la aceptación. | |
| | 4. Valida que la vacante siga activa y que el candidato mantenga su postulación activa. |
| | 5. Registra el Match como confirmado y **cambia automáticamente la AsignacionProyecto** del empleado, sin requerir aprobación de su manager actual. |
| | 6. Actualiza la fecha de inicio en el nuevo rol del empleado, que reinicia el conteo de los 6 meses de antigüedad. |
| | 7. **Notifica únicamente al Analista de RR.HH.** la rotación aceptada. |
| | 8. Informa al empleado que la rotación fue confirmada y que sus rotaciones y mejoras de habilidades aumentan su probabilidad de aparecer en el reporte ejecutivo top 10. |
| | 9. Cancela automáticamente las demás postulaciones activas del empleado (CU_17). |
| | 10. Genera la vacante del puesto que el empleado deja, con activación diferida de 24 horas (CU_18). |
| | 11. Cierra la vacante y notifica el resultado anónimo a los demás candidatos (CU_16). |
| | 12. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. El candidato canceló su postulación o aceptó otra vacante. | a. Informa que el candidato ya no está disponible y regresa al ranking actualizado (CU_14).<br>b. Regresar al paso 1. |
| 4. La vacante ya fue cubierta por otro flujo. | a. Informa que la vacante ya está cerrada y muestra quién la resolvió.<br>b. El caso de uso termina. |
| 7. Falla la notificación a RR.HH. | a. Reintenta el envío; si persiste, deja la rotación registrada, la marca como "pendiente de aviso a RR.HH." y la muestra en el panel de RR.HH.<br>b. Continuar en el paso 8. |
| **CU relacionados** | **CU_14**, **CU_16**, **CU_17**, **CU_18**, CU_21, CU_22 |
| **Precondición** | El manager tiene sesión iniciada, la vacante está activa y el candidato aparece en el ranking de candidatos viables. |
| **Post condición** | Match confirmado, empleado reasignado automáticamente al nuevo proyecto, RR.HH. notificado, postulaciones restantes del empleado canceladas, vacante del puesto liberado creada con activación en 24 horas y vacante original cerrada. |
| **Requerimientos** | RF-23, RF-24, RF-25, RF-26, RF-28 · RN-02, RN-03, RN-09, RN-15, RN-16, RN-22 |

---

### CU_16 — Rechazar candidato y notificar resultado anónimo

| Identificador de Caso Uso | **CU_16** |
|:---|:---|
| **Nombre** | Rechazar candidato y notificar resultado anónimo |
| **Descripción** | Este caso de uso registra el rechazo de un candidato y notifica automáticamente al empleado el puntaje y las habilidades de la persona seleccionada, sin revelar su identidad. El manager no redacta feedback alguno. |
| **Actores** | Manager / Líder Técnico, Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El manager selecciona un candidato del ranking y presiona "Rechazar". | |
| | 2. El sistema solicita confirmación. **No solicita ningún texto ni justificación al manager.** |
| 3. El manager confirma el rechazo. | |
| | 4. Registra el rechazo con la fecha y el Puntaje Final que tenía el candidato en ese momento. |
| | 5. Activa el bloqueo de repostulación de **6 meses** del empleado sobre ese puesto. |
| | 6. Compone la notificación del empleado con: su propio Puntaje Final, el Puntaje Final del candidato seleccionado y las habilidades de ese candidato — **sin nombre, foto, área ni ningún dato identificable**. |
| | 7. Envía la notificación al empleado en menos de 5 minutos, **sin bloquear en ningún momento el cierre de la vacante**. |
| | 8. Incrementa el contador de rechazos a candidatos viables de ese manager y, si llega a 2 o más, dispara la alerta a RR.HH. (CU_24). |
| | 9. El caso de uso termina. |
| **Excepciones** | **Software** |
| 6. La vacante se cerró sin ningún candidato seleccionado. | a. Notifica al empleado que la vacante se cerró sin selección, sin mostrar comparación alguna.<br>b. Continuar en el paso 8. |
| 6. La composición del payload incluiría un dato identificable del seleccionado. | a. Aborta el envío, registra el incidente como falla de privacidad y alerta al Administrador del Sistema.<br>b. El caso de uso termina con error. |
| 7. Falla el envío de la notificación. | a. Reintenta hasta 3 veces y, si persiste, deja la notificación disponible dentro de la aplicación en el historial del empleado (CU_21).<br>b. Continuar en el paso 8. |
| **CU relacionados** | **CU_14**, **CU_15**, CU_21, **CU_24** |
| **Precondición** | El manager tiene sesión iniciada y el candidato aparece en el ranking de la vacante. |
| **Post condición** | Rechazo registrado, empleado notificado con la comparación anónima, bloqueo de 6 meses activo sobre ese puesto y contador de rechazos del manager actualizado. **El cierre de la vacante nunca queda bloqueado.** |
| **Requerimientos** | RF-23, RF-27, RF-32 · RNF-08, RNF-10 · RN-10, RN-17, RN-18, RN-29 |

---

### CU_17 — Cancelar postulaciones restantes

| Identificador de Caso Uso | **CU_17** |
|:---|:---|
| **Nombre** | Cancelar postulaciones restantes |
| **Descripción** | Este caso de uso describe la cancelación automática y silenciosa de las demás postulaciones activas de un empleado cuando acepta una rotación. |
| **Actores** | Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema detecta que un empleado tiene un Match confirmado (CU_15). |
| | 2. Recupera todas sus demás postulaciones en estado "activa". |
| | 3. Cambia el estado de cada una a "cancelada por rotación aceptada". |
| | 4. Retira al empleado del ranking de esas vacantes **sin generar ninguna notificación a los managers afectados**. |
| | 5. Registra la cancelación en el historial del empleado, visible solo para él (CU_21). |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 3. Una de las postulaciones ya fue resuelta por su manager en el mismo ciclo. | a. Mantiene el resultado ya registrado y no lo sobrescribe; la cancelación aplica solo a las postulaciones aún sin resolver.<br>b. Continuar en el paso 4. |
| 2. El empleado no tiene otras postulaciones activas. | a. No realiza ninguna acción.<br>b. El caso de uso termina. |
| **CU relacionados** | **CU_15**, CU_12, CU_14, CU_21 |
| **Precondición** | Existe un Match confirmado para el empleado y al menos una postulación activa adicional. |
| **Post condición** | Las demás postulaciones quedan canceladas y el candidato desaparece de los rankings correspondientes. **Ningún manager recibe notificación del retiro.** |
| **Requerimientos** | RF-26 · RN-09 |

---

### CU_18 — Generar vacante automáticamente

| Identificador de Caso Uso | **CU_18** |
|:---|:---|
| **Nombre** | Generar vacante automáticamente |
| **Descripción** | Este caso de uso describe la creación automática de una vacante cuando un empleado deja una asignación de proyecto, con activación diferida de 24 horas si el origen fue una rotación aceptada. |
| **Actores** | Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema detecta que una AsignacionProyecto quedó libre por renuncia, rotación aceptada, o cierre o creación de un proyecto. |
| | 2. Recupera los datos del puesto liberado: proyecto, área, senioridad y habilidades críticas y deseables. |
| | 3. Crea la Vacante correspondiente, asociada al manager responsable del proyecto. |
| | 4. Si el origen es **renuncia, cierre o creación de proyecto**, activa la vacante de inmediato. |
| | 5. Si el origen es una **rotación aceptada**, deja la vacante en estado "pendiente de activación" y programa su paso a "activa" **24 horas después**. |
| | 6. Al activarse, dispara la comparación con las wishlist de los empleados (CU_08) e inicia el conteo del umbral de días sin match (CU_19). |
| | 7. El caso de uso termina. |
| **Excepciones** | **Software** |
| 3. El puesto liberado no tiene habilidades ni senioridad definidas. | a. Crea la vacante en estado "borrador" y solicita al manager responsable que la complete antes de activarla (CU_09).<br>b. El caso de uso termina. |
| 5. El encargado de supervisar los cambios elimina la vacante dentro de las 24 horas. | a. Cancela la activación programada y marca la vacante como "descartada", registrando quién la descartó.<br>b. El caso de uso termina. |
| 1. La rotación genera una cadena de vacantes. | a. Trata **cada eslabón como una Vacante independiente**, repitiendo este caso de uso por cada puesto liberado. El sistema **no modela la cadena** como entidad.<br>b. Continuar en el paso 2. |
| **CU relacionados** | **CU_15**, CU_09, CU_08, **CU_19**, CU_10 |
| **Precondición** | Una AsignacionProyecto ha quedado libre. |
| **Post condición** | Vacante creada y activa, o programada para activarse en 24 horas. Cada eslabón de una cadena de rotaciones produce su propia vacante. |
| **Requerimientos** | RF-12 · RNF-12 · RN-02, RN-03, RN-04 |

---

### CU_19 — Alertar vacante sin match

| Identificador de Caso Uso | **CU_19** |
|:---|:---|
| **Nombre** | Alertar vacante sin match |
| **Descripción** | Este caso de uso describe la notificación al manager solicitante y al Jefe del área cuando una vacante supera el umbral de días activa sin lograr ningún Match. |
| **Actores** | Sistema, Manager / Líder Técnico |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema revisa periódicamente las vacantes en estado "activa". |
| | 2. Calcula, para cada una, los días transcurridos desde su activación. |
| | 3. Identifica las vacantes que superan el umbral configurado sin Match confirmado. |
| | 4. Compone la alerta con: identificador de la vacante, días abierta, número de postulaciones recibidas y cuántas superaron el 65 %. |
| | 5. Envía la alerta al **manager solicitante** y al **Jefe del área**. |
| 6. El manager o el Jefe del área abre la alerta. | |
| | 7. Muestra el detalle de la vacante y la deja marcada como "en seguimiento". |
| | 8. El caso de uso termina. |
| **Excepciones** | **Software** |
| 3. El umbral de días no está configurado. | a. Aplica el valor por defecto del MVP (30 días) y registra la advertencia para RR.HH. (PEN-01).<br>b. Continuar en el paso 4. |
| 5. La vacante no tiene un Jefe de área asignado. | a. Envía la alerta únicamente al manager solicitante y notifica la inconsistencia al Analista de RR.HH.<br>b. Continuar en el paso 6. |
| 7. El manager decide cerrar la vacante. | a. La marca como "cerrada sin cubrir". **La decisión de contratar externamente o eliminar el puesto queda fuera del alcance de la aplicación.**<br>b. El caso de uso termina. |
| **CU relacionados** | **CU_18**, CU_09, CU_14 |
| **Precondición** | Existe al menos una vacante activa que supera el umbral de días sin Match. |
| **Post condición** | Manager solicitante y Jefe del área notificados. La vacante queda en seguimiento o cerrada sin cubrir. |
| **Requerimientos** | RF-14 · RNF-08, RNF-12 · RN-05, RN-30 · PEN-01 |

---

### CU_20 — Consultar historial de proyectos y rotaciones

| Identificador de Caso Uso | **CU_20** |
|:---|:---|
| **Nombre** | Consultar historial de proyectos y rotaciones |
| **Descripción** | Este caso de uso permite a un manager o a un analista de RR.HH. consultar el historial de proyectos y rotaciones de un empleado con el que tenga relación autorizada. |
| **Actores** | Manager / Líder Técnico, Analista de RR.HH. |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El usuario selecciona un empleado y abre "Historial de proyectos". | |
| | 2. Valida que el solicitante tenga relación autorizada con ese empleado: ser su manager actual, tener una vacante en común con él, o pertenecer a RR.HH. |
| | 3. Recupera los proyectos en los que ha participado, las fechas y las rotaciones confirmadas. |
| | 4. El sistema muestra el historial en orden cronológico, con el número total de rotaciones y los años en la empresa. |
| | 5. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. El solicitante no tiene relación autorizada con el empleado. | a. Deniega el acceso, muestra "No tiene permisos para consultar este perfil" y **registra el intento en la bitácora**.<br>b. El caso de uso termina. |
| 3. El solicitante intenta ver el historial de rechazos del empleado. | a. Lo excluye del resultado: el historial de rechazos es accesible **solo para el propio empleado y para RR.HH.** (CU_21).<br>b. Continuar en el paso 4. |
| 3. El empleado no tiene historial previo. | a. Muestra "Sin rotaciones registradas" junto con su asignación actual.<br>b. El caso de uso termina. |
| **CU relacionados** | CU_21, CU_22, CU_02 |
| **Precondición** | El usuario tiene sesión iniciada y relación autorizada con el empleado consultado. |
| **Post condición** | El historial queda consultado y el acceso registrado. El historial de rechazos **nunca** se expone por esta vía. |
| **Requerimientos** | RF-30 · RNF-07, RNF-10 · RN-21 |

---

### CU_21 — Consultar historial propio de postulaciones

| Identificador de Caso Uso | **CU_21** |
|:---|:---|
| **Nombre** | Consultar historial propio de postulaciones |
| **Descripción** | Este caso de uso permite al empleado revisar todas sus postulaciones, su posición en cada ranking y el resultado anónimo de las que no ganó. |
| **Actores** | Empleado |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El empleado selecciona la opción "Mis postulaciones". | |
| | 2. Valida que el solicitante sea el titular del historial. |
| | 3. Recupera sus postulaciones: activas, aceptadas, rechazadas y canceladas por rotación aceptada. |
| | 4. El sistema muestra cada una con su estado, su Puntaje Final, su posición en el ranking y la fecha. |
| 5. El empleado abre una postulación rechazada. | |
| | 6. Muestra la comparación anónima: su Puntaje Final, el Puntaje Final del seleccionado y las habilidades de esa persona, **sin ningún dato identificable**. |
| | 7. Indica la fecha a partir de la cual podrá volver a postularse a ese puesto, condicionada a que se abra una nueva rotación en él. |
| | 8. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. Un usuario distinto al titular intenta acceder al historial. | a. Deniega el acceso y registra el intento en la bitácora. Solo el propio empleado y RR.HH. pueden consultarlo.<br>b. El caso de uso termina. |
| 6. La vacante se cerró sin seleccionar a nadie. | a. Muestra "Vacante cerrada sin selección" en lugar de la comparación.<br>b. Continuar en el paso 7. |
| 3. El empleado no tiene postulaciones. | a. Muestra "Aún no te has postulado a ninguna vacante" e invita a explorarlas (CU_10).<br>b. El caso de uso termina. |
| **CU relacionados** | CU_12, **CU_16**, CU_17, CU_20 |
| **Precondición** | El empleado tiene sesión iniciada. |
| **Post condición** | El empleado conoce el resultado y la comparación anónima de cada postulación, y sabe cuándo podrá reintentar. |
| **Requerimientos** | RF-31, RF-27 · RNF-07, RNF-10 · RN-10, RN-18 |

---

### CU_22 — Generar reporte ejecutivo top 10

| Identificador de Caso Uso | **CU_22** |
|:---|:---|
| **Nombre** | Generar reporte ejecutivo top 10 |
| **Descripción** | Este caso de uso genera el ranking de los 10 empleados con más habilidades avaladas, más rotaciones y más años en la empresa, como insumo para decisiones de ascenso. |
| **Actores** | Líder Ejecutivo, Analista de RR.HH. |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El líder ejecutivo selecciona la opción "Reporte de recomendación". | |
| | 2. Valida que el solicitante tenga rol Líder Ejecutivo o Analista de RR.HH. |
| | 3. Recupera, por cada empleado, el número de habilidades **en estado "avalado"**, el número de rotaciones confirmadas y los años en la empresa. |
| | 4. Ordena a los empleados según esos tres criterios y conserva los 10 primeros. |
| | 5. El sistema muestra el ranking con el valor de cada criterio visible, e indica explícitamente que es un **insumo de apoyo** y que la decisión de ascenso queda fuera de la aplicación. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 2. El solicitante no tiene el rol requerido. | a. Deniega el acceso y registra el intento en la bitácora.<br>b. El caso de uso termina. |
| 3. Se detectan habilidades en estado "agregado". | a. Las **excluye** del conteo: solo las avaladas cuentan (RN-20).<br>b. Continuar en el paso 4. |
| 4. Hay empate en los tres criterios. | a. Desempata por antigüedad en la empresa y, si persiste, por orden alfabético del identificador de empleado.<br>b. Continuar en el paso 5. |
| **CU relacionados** | **CU_06**, CU_15, CU_20 |
| **Precondición** | El usuario tiene sesión iniciada con rol Líder Ejecutivo o Analista de RR.HH. |
| **Post condición** | Reporte generado y consultado. La aplicación **no registra ni ejecuta** ninguna decisión de ascenso. |
| **Requerimientos** | RF-29 · RNF-07 · RN-20, RN-21, RN-22 |

---

### CU_23 — Gestionar usuarios y roles

| Identificador de Caso Uso | **CU_23** |
|:---|:---|
| **Nombre** | Gestionar usuarios y roles |
| **Descripción** | Este caso de uso permite al administrador crear, activar, desactivar y asignar roles a las cuentas del sistema. |
| **Actores** | Administrador del Sistema |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El administrador selecciona la opción "Gestión de usuarios". | |
| | 2. El sistema muestra las cuentas registradas con su rol y estado. |
| 3. El administrador selecciona una cuenta y le asigna un rol, o cambia su estado. | |
| | 4. Valida que el rol exista y que la operación no deje al sistema sin ningún administrador activo. |
| | 5. El sistema aplica el cambio, registra quién lo realizó y cuándo, y notifica al usuario afectado. |
| | 6. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. La operación dejaría al sistema sin administrador activo. | a. Rechaza el cambio y muestra "Debe existir al menos un administrador activo".<br>b. Regresar al paso 3. |
| 4. El rol indicado no existe. | a. Muestra la lista de roles válidos.<br>b. Regresar al paso 3. |
| 5. Se desactiva una cuenta con postulaciones activas. | a. Cancela sus postulaciones activas sin notificar a los managers y lo informa al administrador.<br>b. Continuar en el paso 6. |
| **CU relacionados** | **CU_01**, CU_02, CU_25 |
| **Precondición** | El administrador tiene sesión iniciada con rol Administrador del Sistema. |
| **Post condición** | Cuenta actualizada con su nuevo rol o estado, y el cambio registrado en la bitácora. |
| **Requerimientos** | RF-03 · RNF-07 |

---

### CU_24 — Alertar rechazos repetidos a RR.HH.

| Identificador de Caso Uso | **CU_24** |
|:---|:---|
| **Nombre** | Alertar rechazos repetidos a RR.HH. |
| **Descripción** | Este caso de uso describe la alerta automática que recibe RR.HH. cuando un mismo manager rechaza a dos o más candidatos viables. Es el mecanismo de control del sistema: sustituye cualquier proceso de apelación del empleado. |
| **Actores** | Sistema, Analista de RR.HH. |
| **Secuencia normal** | |
| **Actor** | **Software** |
| | 1. El sistema registra un rechazo a un candidato con Puntaje Final ≥ 65 % (CU_16). |
| | 2. Incrementa el contador de rechazos a candidatos viables de ese manager. |
| | 3. Verifica si el contador alcanzó el umbral de **2 o más** rechazos. |
| | 4. Compone la alerta con: identificador del manager, vacantes implicadas, Puntaje Final de cada candidato rechazado y del seleccionado en su lugar, y fechas. |
| | 5. Envía la alerta a la bandeja del Analista de RR.HH. en menos de 5 minutos. |
| 6. El analista de RR.HH. abre la alerta. | |
| | 7. Muestra el detalle completo con los datos de los empleados **anonimizados** y marca la alerta como "en revisión". |
| 8. El analista registra el resultado de su revisión. | |
| | 9. El sistema guarda la resolución y cierra la alerta. |
| | 10. El caso de uso termina. |
| **Excepciones** | **Software** |
| 3. El contador no alcanza el umbral. | a. No genera alerta y mantiene el contador actualizado para futuras evaluaciones.<br>b. El caso de uso termina. |
| 4. Los candidatos rechazados pertenecen a vacantes con perfiles muy distintos. | a. Genera igualmente la alerta e incluye esa observación como contexto: la aplicación **reporta el patrón, no lo interpreta ni juzga**.<br>b. Continuar en el paso 5. |
| 7. El analista intenta ver la identidad del candidato seleccionado. | a. La deniega: el anonimato del seleccionado se mantiene también frente a RR.HH. en esta vista (RN-18).<br>b. Continuar en el paso 8. |
| **CU relacionados** | **CU_16**, CU_14, CU_20 |
| **Precondición** | Un manager ha rechazado al menos dos candidatos con Puntaje Final ≥ 65 %. |
| **Post condición** | Alerta generada, revisada por RR.HH. y cerrada con una resolución registrada. |
| **Requerimientos** | RF-32 · RNF-06, RNF-08, RNF-10 · RN-27, RN-29 |

---

### CU_25 — Configurar pesos, umbrales y escalera de carrera

| Identificador de Caso Uso | **CU_25** |
|:---|:---|
| **Nombre** | Configurar pesos, umbrales y escalera de carrera |
| **Descripción** | Este caso de uso permite a RR.HH. y a VP Engineering ajustar los parámetros del algoritmo y los requisitos de cada nivel de la escalera de carrera, sin recompilar el sistema. |
| **Actores** | Analista de RR.HH., VP Engineering |
| **Secuencia normal** | |
| **Actor** | **Software** |
| 1. El usuario selecciona la opción "Configuración del algoritmo". | |
| | 2. El sistema muestra los parámetros vigentes: pesos (0.4 / 0.6), umbral de viabilidad (65 %), ciclo de evaluación (3 días), antigüedad mínima (6 meses), bloqueo por rechazo (6 meses), activación diferida (24 horas) y umbral de días sin match. |
| 3. El usuario modifica los parámetros que necesita ajustar. | |
| | 4. Valida que los pesos sumen 1.0, que el umbral esté entre 0 y 100, y que los plazos sean positivos. |
| 5. El VP Engineering edita los requisitos técnicos de un nivel de la escalera de carrera. | |
| | 6. Valida que cada requisito corresponda a una habilidad del catálogo. |
| | 7. El sistema guarda la configuración, registra quién la cambió y cuándo, y aplica los nuevos valores a los cálculos **posteriores**. |
| | 8. El caso de uso termina. |
| **Excepciones** | **Software** |
| 4. Los pesos no suman 1.0. | a. Rechaza el cambio y muestra "Match Score + Career Impact deben sumar 1.0".<br>b. Regresar al paso 3. |
| 4. El umbral de viabilidad está fuera del rango 0–100. | a. Muestra el rango válido.<br>b. Regresar al paso 3. |
| 7. Existen postulaciones activas calculadas con la configuración anterior. | a. **Conserva** los puntajes ya registrados en la bitácora —el ranking de una vacante en curso no cambia retroactivamente— y aplica la nueva configuración solo a los cálculos posteriores.<br>b. Continuar en el paso 8. |
| 6. Un requisito no existe en el catálogo de habilidades. | a. Señala el requisito no reconocido y ofrece darlo de alta.<br>b. Regresar al paso 5. |
| **CU relacionados** | **CU_11**, CU_04, CU_13, CU_19, CU_23 |
| **Precondición** | El usuario tiene sesión iniciada con rol Analista de RR.HH. o VP Engineering. |
| **Post condición** | Configuración actualizada y aplicada a los cálculos posteriores. Los puntajes históricos permanecen intactos en la bitácora. |
| **Requerimientos** | RF-33 · RNF-06, RNF-12 · RN-13, RN-24, RN-27 · PEN-01, PEN-03 |

---

## 3. Flujo principal del sistema

```
CU_01 Registrarse --> CU_02 Iniciar sesion
                           |
        +------------------+-------------------+
        |                                      |
     EMPLEADO                              MANAGER
        |                                      |
  CU_03 Perfil                          CU_09 Publicar vacante
  CU_04 Meta de carrera                        |
  CU_05 Agregar habilidad ---> CU_06 Avalar habilidad
  CU_07 Wishlist                               |
        |                                      |
  CU_08 Notificacion proactiva <---------------+
        |
  CU_10 Buscar vacantes
        |
  CU_11 Calcular Puntaje Final  (nucleo del sistema)
        |
  CU_12 Postularse   -- si < 65% --> advierte, pero no bloquea
        |
        v
  CU_14 Revisar ranking (manager, cada 3 dias)   + CU_13 Costo de rotacion
        |
        +--------------------+---------------------+
        |                                          |
  CU_15 ACEPTAR                              CU_16 RECHAZAR
        |                                          |
        +--> transicion automatica                 +--> notificacion anonima al empleado
        +--> notifica solo a RRHH                  +--> bloqueo de 6 meses
        +--> CU_17 cancela otras postulaciones     +--> CU_24 alerta a RRHH si 2+ rechazos
        +--> CU_18 genera vacante (+24h)
        +--> aviso del incentivo (CU_22)
             |
             +--> CU_19 alerta si la vacante no logra match

TRANSVERSALES: CU_20 historial de proyectos · CU_21 historial propio ·
               CU_22 reporte ejecutivo · CU_23 usuarios y roles · CU_25 configuracion
```

---

## 4. Matriz de trazabilidad

### 4.1 Caso de uso → Requerimientos → Reglas de negocio

| CU | Nombre | Actor principal | RF | RNF | RN |
|---|---|---|---|---|---|
| CU_01 | Registrarse | Usuario | RF-01 | RNF-07, RNF-13 | — |
| CU_02 | Iniciar sesión | Todos | RF-02 | RNF-07 | — |
| CU_03 | Gestionar perfil profesional | Empleado | RF-04 | — | RN-07, RN-23 |
| CU_04 | Definir meta de carrera | Empleado | RF-05 | — | RN-24 |
| CU_05 | Agregar habilidad al perfil | Empleado | RF-06 | — | RN-19, RN-20 |
| CU_06 | Avalar habilidad del equipo | Manager | RF-07, RF-08 | — | RN-19, RN-20 |
| CU_07 | Gestionar lista de deseos | Empleado | RF-09 | — | — |
| CU_08 | Notificar vacante por wishlist | Sistema | RF-10 | RNF-08 | RN-01, RN-07, RN-10 |
| CU_09 | Publicar vacante interna | Manager | RF-11 | — | RN-01, RN-11 |
| CU_10 | Buscar y filtrar vacantes | Empleado | RF-13 | RNF-03, RNF-04 | RN-03, RN-11 |
| CU_11 | **Calcular Puntaje Final** | Sistema | RF-08, RF-15, RF-16 | RNF-06, RNF-09, RNF-12 | RN-13, RN-20, RN-23, RN-24, RN-25 |
| CU_12 | Postularse a una vacante | Empleado | RF-17, RF-19, RF-20 | — | RN-06…RN-10, RN-28 |
| CU_13 | Consultar costo de rotación | Manager | RF-18 | — | RN-26 |
| CU_14 | Revisar ranking de candidatos | Manager | RF-21, RF-22 | RNF-03, RNF-04, RNF-09 | RN-12, RN-13, RN-14, RN-27 |
| CU_15 | Aceptar candidato y ejecutar rotación | Manager | RF-23, RF-24, RF-25, RF-26, RF-28 | — | RN-02, RN-03, RN-09, RN-15, RN-16, RN-22 |
| CU_16 | Rechazar candidato y notificar resultado | Manager / Sistema | RF-23, RF-27, RF-32 | RNF-08, RNF-10 | RN-10, RN-17, RN-18, RN-29 |
| CU_17 | Cancelar postulaciones restantes | Sistema | RF-26 | — | RN-09 |
| CU_18 | Generar vacante automáticamente | Sistema | RF-12 | RNF-12 | RN-02, RN-03, RN-04 |
| CU_19 | Alertar vacante sin match | Sistema | RF-14 | RNF-08, RNF-12 | RN-05, RN-30 |
| CU_20 | Consultar historial de proyectos | Manager / RR.HH. | RF-30 | RNF-07, RNF-10 | RN-21 |
| CU_21 | Consultar historial propio | Empleado | RF-27, RF-31 | RNF-07, RNF-10 | RN-10, RN-18 |
| CU_22 | Generar reporte ejecutivo top 10 | Líder Ejecutivo | RF-29 | RNF-07 | RN-20, RN-21, RN-22 |
| CU_23 | Gestionar usuarios y roles | Administrador | RF-03 | RNF-07 | — |
| CU_24 | Alertar rechazos repetidos | Sistema / RR.HH. | RF-32 | RNF-06, RNF-08, RNF-10 | RN-27, RN-29 |
| CU_25 | Configurar pesos y umbrales | RR.HH. / VP Eng. | RF-33 | RNF-06, RNF-12 | RN-13, RN-24, RN-27 |

### 4.2 Cobertura de los requerimientos funcionales

| RF | Realizado por | RF | Realizado por |
|---|---|---|---|
| RF-01 | CU_01 | RF-18 | CU_13 |
| RF-02 | CU_02 | RF-19 | CU_12 |
| RF-03 | CU_23 | RF-20 | CU_12 |
| RF-04 | CU_03 | RF-21 | CU_14 |
| RF-05 | CU_04 | RF-22 | CU_14 |
| RF-06 | CU_05 | RF-23 | CU_15, CU_16 |
| RF-07 | CU_06 | RF-24 | CU_15 |
| RF-08 | CU_06, CU_11 | RF-25 | CU_15 |
| RF-09 | CU_07 | RF-26 | CU_15, CU_17 |
| RF-10 | CU_08 | RF-27 | CU_16, CU_21 |
| RF-11 | CU_09 | RF-28 | CU_15 |
| RF-12 | CU_18 | RF-29 | CU_22 |
| RF-13 | CU_10 | RF-30 | CU_20 |
| RF-14 | CU_19 | RF-31 | CU_21 |
| RF-15 | CU_11 | RF-32 | CU_16, CU_24 |
| RF-16 | CU_11, CU_12 | RF-33 | CU_25 |
| RF-17 | CU_12 | | |

**Cobertura: 33 de 33 RF (100 %).** Ningún requerimiento funcional queda sin caso de uso, y
ningún caso de uso existe sin un requerimiento que lo justifique.

### 4.3 Cobertura de las historias de usuario

| HU | Casos de uso que la realizan |
|---|---|
| HU-001 Revisar el ranking de candidatos | CU_11, CU_13, CU_14 |
| HU-002 Detectar patrones de rechazo sesgados | CU_16, CU_24 |
| HU-003 Perfil anónimo del seleccionado | CU_16, CU_21 |
| HU-004 Competir por méritos | CU_11, CU_14, CU_15, CU_16, CU_21, CU_24 |
| HU-005 Tiempo de respuesta | CU_10, CU_11, CU_14 *(atributo de calidad: RNF-01, RNF-04)* |
| HU-006 Protección de datos sensibles | CU_01, CU_02, CU_20, CU_21, CU_23 |
| HU-007 Avalar habilidades del equipo | CU_05, CU_06, CU_11 |
| HU-008 Postularme con reglas claras | CU_12, CU_17 |
| HU-009 Vacante automática al liberar un puesto | CU_18, CU_19 |
| HU-010 Reporte ejecutivo top 10 | CU_15, CU_22 |

---

## 5. Casos de uso críticos para la validación

Antes de pasar a producción deben validarse con especial rigor:

| CU | Por qué es crítico | Qué probar |
|---|---|---|
| **CU_11** Calcular Puntaje Final | Es el núcleo del sistema: de él dependen CU_08, CU_10, CU_12 y CU_14. | Que solo entren habilidades **avaladas**; que ningún dato personal intervenga (RNF-09); que **todo** cálculo quede en la bitácora (RNF-06). |
| **CU_16** Rechazar y notificar | Concentra la regla de negocio más sensible del producto. | Que **ninguna** respuesta del API exponga la identidad del seleccionado; que el cierre de la vacante **nunca** quede bloqueado. |
| **CU_14** Revisar ranking | Es donde se materializa la promesa de decisión por mérito. | Que el ranking sea **inmutable** en UI y en API; que el filtro de 65 % funcione en ambos extremos. |
| **CU_15** Aceptar y ejecutar rotación | Dispara cinco efectos en cascada. | Que se ejecuten los cinco (transición, aviso a RR.HH., cancelación, vacante +24 h, cierre) y que ninguno quede a medias ante un fallo. |
| **CU_18** Generar vacante automáticamente | Sostiene la continuidad del modelo de rotación. | Que una cadena de 3 rotaciones genere 3 vacantes independientes; que la ventana de 24 h se respete. |

---

## 6. Pendientes que afectan a los casos de uso

| Pendiente | Casos de uso afectados | Supuesto del MVP |
|---|---|---|
| **PEN-01** Umbral de días sin match | CU_19, CU_25 | 30 días, igual para todos los roles. |
| **PEN-02** Alcance del Costo de Rotación | CU_13, CU_14 | Se muestra como métrica **informativa**; no altera el Puntaje Final ni el orden del ranking. |
| **PEN-03** Definición de la escalera de carrera | CU_04, CU_11, CU_25 | Requisitos con peso igual, definidos por VP Engineering junto a RR.HH. |

Cada supuesto queda registrado aquí y en las
[Reglas de Negocio §9](../reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md#9-pendientes-por-confirmar-con-la-empresa).
Al confirmarse con la empresa, deben actualizarse **ambos** documentos.
