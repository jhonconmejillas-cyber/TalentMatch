# TalentMatch — Historias de Usuario

| Campo | Valor |
|---|---|
| **Documento** | Historias de Usuario (HU) |
| **Versión** | 2.1 — corregida contra las [Reglas de Negocio v2.1](../reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) |
| **Fecha** | 2026-09-21 |
| **Curso** | Fundamentos de Ingeniería de Software (FIS 2610) — Pontificia Universidad Javeriana |
| **Total** | 10 historias (6 funcionales, 2 no funcionales, 2 de soporte al modelo de rotación) |

## Equipo

| Integrante | Rol(es) en el equipo | GitHub |
|---|---|---|
| Carlos Camacho | Scrum Master / DevOps Engineer | github.com/charly-31 |
| Jhon Mejia | Product Owner / Configuration Manager | github.com/jhonconmejillas-cyber |
| Juan Maldonado | Sprint Planner / QA Lead | github.com/camilo058 |

---

## Nota de alineación con las Reglas de Negocio (v2.0)

La versión 1.0 de estas historias se escribió sobre una lectura del wiki que hoy está
**derogada**. Concretamente, asumía que:

1. el Manager debía dar **feedback obligatorio** al rechazar a un candidato viable, y que el
   sistema **bloqueaba el cierre de la vacante** hasta recibirlo;
2. el empleado esperaba **3 meses** para repostularse;
3. existía un **flujo de apelación** ante un comité de RR.HH. + VP Engineering;
4. las vacantes se abrían en **oleadas trimestrales**.

Las Reglas de Negocio vigentes establecen lo contrario: el rechazo **no genera feedback del
manager** (RN-17), el empleado ve el **% y las habilidades del seleccionado sin su nombre**
(RN-18), el bloqueo de repostulación es de **6 meses** y exige una nueva rotación en el puesto
(RN-10), **no hay apelación** —el control es la alerta automática a RR.HH. por rechazos
repetidos (RN-29)— y el modelo de rotación es **continuo, sin ventanas** (RN-01).

Las 6 historias originales se corrigieron conservando su intención, y se agregaron 4 historias
que cubren reglas sin representar: el **aval de habilidades** (RN-19/RN-20), las **validaciones
de postulación** (RN-07…RN-10), la **generación automática de vacantes** (RN-02/RN-03) y el
**reporte ejecutivo top 10** (RN-21/RN-22).

---

# HU-001 — Revisar el ranking de candidatos internos para decidir rápido

**Tipo:** Historia funcional

> **COMO** Manager / Líder Técnico,
> **QUIERO** ver el ranking de empleados que se postularon a mi vacante interna, ordenado por
> Puntaje Final (Match Score), sin revisar perfiles uno por uno,
> **PARA** identificar al mejor candidato interno en minutos y avanzar la rotación sin burocracia.

## Descripción

Tengo una vacante interna abierta en mi equipo de Backend. Ocho empleados de otras áreas se
postularon para moverse a mi proyecto — TalentMatch permite rotar **entre áreas distintas**
(RN-11). No tengo tiempo de revisar cada perfil a fondo. Necesito ver de inmediato quién tiene
el mejor Puntaje Final —el Match Score calculado sobre habilidades **avaladas** y senioridad—:
🟢 Empleado_001 85 %,
🟡 Empleado_002 72 %, 🔴 Empleado_003 68 %. El sistema solo me muestra candidatos con Puntaje
Final **≥ 65 %** (RN-27); los que quedan por debajo ni siquiera aparecen en mi lista. Cada tres
días el sistema me habilita el lote de peticiones acumuladas para resolver (RN-12).

## Criterios de Aceptación

- **Escenario 1 — Ranking visible al abrir la vacante.** *Dado* que abro una vacante con
  empleados postulados que superan el umbral de viabilidad (≥ 65 %), *cuando* carga la página,
  *entonces* veo el ranking **ordenado por Puntaje Final descendente** con indicador de color,
  en menos de 2 segundos.
- **Escenario 2 — Desglose sin salir del ranking.** *Dado* que veo el ranking, *cuando* hago
  clic en un candidato, *entonces* veo su desglose (Match Score y Costo de Rotación estimado)
  **sin perder el contexto** de los demás candidatos.
- **Escenario 3 — El orden no se puede manipular.** *Dado* que el ranking está en pantalla,
  *cuando* intento arrastrar, reordenar o fijar un candidato, *entonces* la interfaz no lo
  permite y el backend rechaza cualquier petición de reordenamiento (RN-14).

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] El Puntaje Final se calcula como `Puntaje Final = Match Score` y filtra correctamente con el umbral de 65 %.
- [ ] Solo se consideran habilidades en estado `avalado` (RN-20), verificado con prueba unitaria.
- [ ] Probado con hasta 20 candidatos por vacante sin superar los 2 s de carga (RNF-04).
- [ ] Sin errores críticos.
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta (Crítica)**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Endpoint API que devuelve los candidatos postulados con su Puntaje Final calculado. | Backend |
| 2 | Consulta que une postulación y habilidades **avaladas**. | Configuration Manager |
| 3 | Filtro de umbral (Puntaje Final ≥ 65 %) en el backend. | Backend |
| 4 | Componente de lista rankeada con indicador de color en el frontend. | Frontend |
| 5 | Vista de desglose (Match Score / Costo de Rotación). | Frontend |
| 6 | Bloqueo de reordenamiento manual en UI y en API. | Backend / Frontend |
| 7 | Pruebas unitarias del cálculo, del filtro de umbral y de la inmutabilidad del orden. | QA Lead |
| 8 | Pipeline de CI que corre las pruebas antes de cada merge. | DevOps Engineer |

## Discusión

El Manager no quiere reportes complejos: quiere una lista priorizada, color + número, para
decidir rápido. Es el pilar de **transparencia en el match**: todo el cálculo es explicable, sin
caja negra. El bloqueo del reordenamiento es lo que convierte la lista en una decisión por mérito
y no en una preferencia personal.

## Trabajo Relacionado

CU_14 (Revisar ranking) · CU_11 (Calcular Puntaje Final) · RF-15, RF-21, RF-22 · RNF-03, RNF-04, RNF-09 · RN-12, RN-13, RN-14, RN-27

### Qué cambió frente a la v1.0

El Escenario 3 original decía que al aceptar *"los demás candidatos reciben notificación de
cierre"*. Se reemplazó por la **inmutabilidad del ranking** (RN-14), y la notificación al
rechazado se trasladó a **HU-003**, donde se describe con la regla correcta (perfil anónimo del
seleccionado, RN-18).

---

# HU-002 — Detectar patrones de rechazo sesgados mes a mes

**Tipo:** Historia funcional

> **COMO** Analista de RR.HH.,
> **QUIERO** recibir un reporte mensual automático que muestre si algún Manager presenta patrones
> de rechazo hacia candidatos internos viables (Puntaje Final ≥ 65 %),
> **PARA** detectar y corregir sesgos antes de que un empleado se sienta discriminado.

## Descripción

Como Analista de RR.HH. debo verificar el cumplimiento de las políticas de equidad. El sistema ya
genera una **alerta puntual** cuando un Manager rechaza 2 o más candidatos viables (RN-29);
necesito además un reporte **mensual consolidado** con patrones agregados por Manager, por
ejemplo: *"Manager A rechazó al 80 % de los candidatos con Puntaje Final ≥ 65 % en los últimos
3 meses"*. Esto me permite intervenir con datos objetivos, sin señalar culpables: solo patrones.

## Criterios de Aceptación

- **Escenario 1 — Reporte automático.** *Dado* que es el primer día del mes, *cuando* el sistema
  ejecuta su proceso nocturno, *entonces* genera un archivo descargable con la tasa de
  aceptación/rechazo por Manager, incluyendo su tasa de rechazo a candidatos ≥ 65 %.
- **Escenario 2 — Detectar anomalías.** *Dado* que un Manager presenta una desviación mayor al
  20 % respecto al promedio de la organización, *cuando* abro el reporte, *entonces* veo su fila
  resaltada y puedo abrir el detalle de los rechazos que generaron la anomalía, **con el Puntaje
  Final de cada candidato rechazado y el del seleccionado** en su lugar.
- **Escenario 3 — Datos anonimizados.** *Dado* que el reporte incluye datos sensibles, *cuando*
  lo genero, *entonces* los nombres de empleados aparecen anonimizados (Empleado_001,
  Empleado_002), los IDs de Manager sí aparecen, y las alertas de rechazo repetido (RN-29) quedan
  enlazadas al detalle.

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] El reporte se genera cada mes sin intervención manual.
- [ ] Las reglas de anonimización fueron validadas: no exponen información identificable.
- [ ] Probado con datos simulados de al menos 20 managers, sin errores críticos.
- [ ] Validada por Jhon Mejia (Product Owner) y revisada por Juan Maldonado (QA Lead).

## Prioridad

**Media** — el control obligatorio por regla de negocio es la alerta de RN-29 (**HU-002 lo
complementa**, no lo sustituye).

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Agregación de tasa de aceptación/rechazo por Manager y desviación vs. el promedio. | Backend |
| 2 | Job programado que corre el primer día de cada mes. | DevOps Engineer |
| 3 | Anonimización de nombres de empleados en la capa de reporte. | Backend |
| 4 | Archivo descargable con resaltado condicional para desviaciones > 20 %. | Backend |
| 5 | Enlazar las alertas de "2+ rechazos a candidato viable" al detalle del reporte. | Configuration Manager |
| 6 | Pruebas de integración del job mensual y de las reglas de anonimización. | QA Lead |

## Discusión

El reporte no debe culpar a nadie, solo ser objetivo: *"X sucedió, X veces"*. Se alimenta de la
**bitácora de cálculo** (RNF-06), que es la única fuente confiable de qué puntaje tenía cada
candidato en el momento de la decisión.

## Trabajo Relacionado

CU_24 (Alertar rechazos repetidos) · CU_11 · RF-32, RF-15 · RNF-06, RNF-09, RNF-10 · RN-29

### Qué cambió frente a la v1.0

El Escenario 2 exigía verificar *"si dejó el feedback obligatorio"*. Como el feedback del manager
**ya no existe** (RN-17), el detalle de la anomalía ahora muestra los **puntajes comparados**
(rechazado vs. seleccionado), que es la evidencia objetiva de si el rechazo fue por mérito.
La prioridad bajó de *Alta (Crítica)* a *Media* porque el control exigido por la regla de negocio
es la alerta automática, no el reporte mensual.

---

# HU-003 — Saber con qué perfil perdí la vacante, sin exponer a nadie

**Tipo:** Historia funcional

> **COMO** Empleado (Ingeniero Junior),
> **QUIERO** que, al ser rechazado en una vacante interna, el sistema me muestre el puntaje y las
> habilidades de la persona seleccionada **sin revelar su identidad**,
> **PARA** entender objetivamente qué me faltó y qué habilidad desarrollar para mi próxima
> oportunidad, sin depender de que mi Manager me escriba nada.

## Descripción

Soy un ingeniero junior con 1 año de experiencia. Me postulé a una vacante interna de Backend
Senior, mi Puntaje Final fue 68 % (por encima del umbral de 65 %), y el Manager eligió a otra
persona. **No espero un texto del Manager**: las reglas de TalentMatch establecen que el manager
no redacta feedback (RN-17). Lo que sí me da el sistema es la comparación objetiva: el
seleccionado tuvo 84 % y tiene Kubernetes y Go avalados, que yo no tengo. Eso me dice
exactamente qué desarrollar, y lo hace **sin exponer a la otra persona** (RN-18). También quiero
saber cuándo podría volver a intentarlo en ese puesto.

## Criterios de Aceptación

- **Escenario 1 — Notificación automática al cerrarse la vacante.** *Dado* que fui rechazado en
  una vacante, *cuando* el Manager confirma su decisión, *entonces* el sistema genera mi
  notificación **automáticamente y en menos de 5 minutos**, sin que el Manager tenga que escribir
  ni aprobar nada, y **sin bloquear** el cierre de la vacante.
- **Escenario 2 — Comparación anónima.** *Dado* que abro la notificación, *entonces* veo:
  *"Tu Puntaje Final: 68 % · Puntaje del seleccionado: 84 %"* y una tabla
  `Habilidad | ¿La tengo avalada? | ¿La tiene el seleccionado?`. **En ninguna parte aparece el
  nombre, la foto, el área ni ningún dato que identifique a la persona seleccionada.**
- **Escenario 3 — Qué sigue.** *Dado* que veo mi brecha en una habilidad concreta (p. ej.
  Kubernetes), *cuando* cierro la notificación, *entonces* el sistema me ofrece agregarla a mis
  **deseos (wishlist)** y me indica que podré volver a postularme a ese puesto cuando pasen
  **6 meses** **y** se abra una nueva rotación en él (RN-10).

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] Ninguna respuesta del API de esta pantalla incluye identificadores del seleccionado
      (validado con prueba automática sobre el JSON de respuesta).
- [ ] El cierre de la vacante **nunca** queda bloqueado esperando acción del Manager.
- [ ] La regla de los 6 meses + nueva rotación se valida antes de habilitar la repostulación.
- [ ] Probado con al menos 3 combinaciones distintas de brechas de habilidades.
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Generar la notificación de resultado automáticamente al registrarse la decisión. | Backend |
| 2 | Construir el payload anónimo (puntaje + habilidades del seleccionado, **sin** identificadores). | Backend |
| 3 | Vista de comparación de brechas en el frontend. | Frontend |
| 4 | Sugerencia de agregar la habilidad faltante a la wishlist. | Backend |
| 5 | Implementar el bloqueo de repostulación: 6 meses **y** nueva rotación en el puesto. | Configuration Manager |
| 6 | Prueba automática que falla si la respuesta filtra cualquier dato del seleccionado. | QA Lead |
| 7 | Configurar el canal de notificación (correo / in-app). | DevOps Engineer |

## Discusión

Los ingenieros junior son los más sensibles al rechazo, y la v1.0 intentaba resolverlo pidiéndole
al Manager un texto empático. En la práctica eso genera dos problemas: **fricción** (la vacante
se queda bloqueada esperando a un Manager ocupado) y **subjetividad** (el texto puede ser
inconsistente o incluso sesgado). La regla vigente lo resuelve mejor: una comparación numérica
y objetiva que llega sola, es igual para todos, y protege la privacidad de quien sí fue elegido.

## Trabajo Relacionado

CU_16 (Rechazar candidato y notificar resultado) · CU_21 (Consultar historial propio) · RF-27, RF-19 · RNF-08, RNF-09, RNF-10 · RN-10, RN-17, RN-18

### Qué cambió frente a la v1.0

Esta historia se **reescribió por completo**. La v1.0 se titulaba *"Recibir Feedback Obligatorio
al Ser Rechazado"* y exigía que el sistema **bloqueara el cierre de la vacante** hasta que el
Manager completara un formulario, con un SLA de 48 horas y una espera de 3 meses para
repostularse. Los cuatro elementos contradicen las reglas vigentes: no hay feedback (RN-17), no
hay bloqueo (RN-17), la notificación es automática e inmediata (RNF-08) y la espera es de
**6 meses** más una nueva rotación en el puesto (RN-10).

---

# HU-004 — Competir por vacantes internas por méritos, no por cercanía

**Tipo:** Historia funcional

> **COMO** Empleado (Ingeniero Senior),
> **QUIERO** competir por vacantes internas en igualdad de condiciones, con un ranking objetivo
> por Puntaje Final y sin que mi relación personal con el Manager influya,
> **PARA** asegurar que mi movilidad interna dependa de mis habilidades, no
> de la política interna.

## Descripción

Tengo 8 años en la empresa, pero cambié de área hace 6 meses y el Manager de la vacante no me
conoce. Cuando aparece una vacante interna de Cloud Engineer, quiero que me evalúen objetivamente:
si mi Puntaje Final es 82 % y otro empleado tiene 75 %, el sistema debe mostrarme primero, sin
que el Manager pueda reordenar la lista ni ignorar mi perfil por no estar "en su círculo". El
cálculo solo puede usar mis **habilidades avaladas** y mi senioridad: ni mi nombre, ni mi edad,
ni cuánto me conoce el Manager (RNF-09).

## Criterios de Aceptación

- **Escenario 1 — Ranking transparente e inmutable.** *Dado* que hay una vacante interna de
  Cloud Engineer, *cuando* el Manager abre el panel de candidatos, *entonces* ve a los empleados
  postulados **ordenados por Puntaje Final descendente**, sin posibilidad alguna de reordenarlos
  manualmente (RN-14).
- **Escenario 2 — Historial visible para mí.** *Dado* que fui el candidato #1 en Puntaje Final,
  *cuando* la vacante se cierra, *entonces* puedo ver en mi historial *"Fuiste #1 de 12
  candidatos"* junto con el **puntaje y las habilidades de la persona seleccionada, sin su
  nombre** (RN-18).
- **Escenario 3 — Control automático ante un patrón de rechazo.** *Dado* que un mismo Manager
  rechazó a 2 o más candidatos viables (≥ 65 %), *cuando* se registra el segundo rechazo,
  *entonces* el sistema **emite automáticamente una alerta a RR.HH.** con el detalle de los
  puntajes comparados, sin que ningún empleado tenga que reclamar (RN-29).

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] El ranking es inmutable: existe prueba automática de que la API rechaza todo reordenamiento.
- [ ] Existe prueba unitaria de que ningún campo personal entra al cálculo del Puntaje Final.
- [ ] El historial de posición y el resultado anónimo persisten y son consultables por el empleado.
- [ ] La alerta de rechazos repetidos se dispara de extremo a extremo en ambiente de pruebas.
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta (Crítica)**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Bloquear en el backend cualquier reordenamiento manual del ranking. | Backend |
| 2 | Lista cerrada de campos admitidos en el cálculo del Puntaje Final (RNF-09). | Backend |
| 3 | Persistir la posición del candidato y el resultado anónimo por vacante. | Backend |
| 4 | Vista "Mis postulaciones" para el empleado. | Frontend |
| 5 | Contador de rechazos a candidatos viables por Manager y disparo de la alerta. | Backend |
| 6 | Bandeja de alertas para el Analista de RR.HH. | Frontend |
| 7 | Pruebas de integridad del ranking y del disparo de la alerta. | QA Lead |
| 8 | Permisos de acceso de RR.HH. a la bandeja de alertas. | DevOps Engineer |

## Discusión

Este es el corazón de TalentMatch. Un ingeniero senior sabe que la "rosca" existe. La v1.0 le
daba un **recurso de apelación**; el modelo vigente es mejor porque no exige que la persona
rechazada dé la pelea: el sistema **detecta el patrón solo** y escala a RR.HH. (RN-29). El
empleado no tiene que exponerse ante su Manager para que se revise una decisión.

## Trabajo Relacionado

CU_14, CU_15, CU_16, CU_21, CU_24 · RF-15, RF-21, RF-27, RF-31, RF-32 · RNF-06, RNF-09 · RN-13, RN-14, RN-18, RN-29

### Qué cambió frente a la v1.0

El Escenario 2 mencionaba *"el motivo de rechazo registrado como parte del feedback obligatorio"*
→ ahora muestra el **perfil anónimo del seleccionado** (RN-18). El Escenario 3 describía una
**apelación ante un comité de RR.HH. + VP Engineering** → se reemplazó por la **alerta automática
a RR.HH.** (RN-29), que es el mecanismo que las Reglas de Negocio sí contemplan. Las subtareas
de "flujo de apelación" y "modelo de datos de apelación" se eliminaron en consecuencia.

---

# HU-005 — Tiempo de respuesta del ranking y del cálculo de Puntaje Final

**Tipo:** Historia no funcional — Rendimiento

> **COMO** Empleado o Manager,
> **QUIERO** que el ranking de candidatos y el cálculo del Puntaje Final se carguen en menos de
> 2 segundos, aun con varias vacantes abiertas al mismo tiempo,
> **PARA** no perder tiempo ni interés al usar la plataforma.

## Descripción

TalentMatch depende de que Managers y Empleados revisen rankings y resultados de match de forma
inmediata; cualquier demora hace que la plataforma se sienta más lenta que los métodos manuales
de siempre y reduce su adopción. Esta historia no agrega funcionalidad: garantiza que HU-001 y
HU-004 cumplan un estándar de rendimiento **medible y alcanzable en el MVP**. Como el modelo de
rotación es **continuo** (RN-01), la carga es sostenida a lo largo del año, no concentrada en
picos trimestrales.

## Criterios de Aceptación

- **Escenario 1 — Carga normal.** *Dado* que hay hasta **20 candidatos** postulados a una
  vacante, *cuando* el Manager abre el ranking, *entonces* la página carga completa —incluido el
  cálculo del Puntaje Final— en **menos de 2 segundos**.
- **Escenario 2 — Carga máxima del MVP.** *Dado* que **50 usuarios** usan la plataforma al mismo
  tiempo, *cuando* se generan rankings en paralelo, *entonces* el **90 %** de las respuestas se
  entrega en **menos de 3 segundos**.
- **Escenario 3 — El usuario nunca queda sin señal.** *Dado* que una respuesta tarda más de lo
  normal, *cuando* supera los **300 ms**, *entonces* la interfaz muestra un indicador de carga
  para que el usuario no perciba que la aplicación se congeló.

## Definición de Terminado

- [ ] Cumple los 3 escenarios en una prueba de carga con las cifras indicadas.
- [ ] Los tiempos de respuesta quedan registrados en el log de la aplicación y son consultables.
- [ ] Sin errores ni caídas del servicio durante la prueba de carga.
- [ ] Validada por Jhon Mejia (Product Owner) con la evidencia de la prueba.

## Prioridad

**Media**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Documentar las metas de tiempo de respuesta (RNF-04) en el archivo de configuración. | Product Owner / QA Lead |
| 2 | Guardar el Puntaje Final calculado y recalcularlo solo si cambia el perfil o la vacante. | Backend |
| 3 | Índices de base de datos sobre postulación, habilidad avalada y vacante. | Configuration Manager |
| 4 | Prueba de carga simulando 50 usuarios concurrentes. | QA Lead |
| 5 | Indicador de carga en el frontend para respuestas > 300 ms. | Frontend |
| 6 | Registro de tiempos de respuesta en el log de la aplicación. | DevOps Engineer |

## Discusión

Si el ranking tarda, el Manager vuelve a los métodos manuales y la plataforma pierde su propuesta
de valor de *"decidir en minutos"*. Esta historia traduce esa expectativa en números que el
equipo puede probar con las herramientas del curso.

## Trabajo Relacionado

RNF-01, RNF-04, RNF-08 · HU-001, HU-004 · RN-01

### Qué cambió frente a la v1.0

Las metas se **simplificaron** (ver §2.2 del documento de requerimientos): 500 usuarios
concurrentes → **50**; p95 < 4 s → **90 % < 3 s**; *"panel de monitoreo con métricas p50/p95"*
→ **registro de tiempos en el log de la aplicación**. Además se eliminó la referencia a la
*"apertura trimestral de vacantes"*, que contradice el modelo de rotación continuo (RN-01).

---

# HU-006 — Protección de los datos sensibles de los empleados

**Tipo:** Historia no funcional — Seguridad y Privacidad

> **COMO** Empleado,
> **QUIERO** que mis habilidades, mis postulaciones y mis rechazos estén
> protegidos con autenticación y permisos por rol,
> **PARA** que ninguna persona no autorizada —ni siquiera otro Manager— vea información sobre mi
> carrera que no le corresponde.

## Descripción

TalentMatch maneja información sensible: hacia dónde quiere crecer un empleado y en qué vacantes
no fue elegido. Si un Manager pudiera ver que su gente está explorando otras vacantes, o si un
empleado pudiera ver el desglose de Puntaje Final de otro, se rompería la confianza que sostiene
todo el producto. A esto se suma la regla de anonimato de RN-18: el sistema **nunca** puede
revelar quién fue seleccionado en una vacante.

## Criterios de Aceptación

- **Escenario 1 — Permisos por rol.** *Dado* que un Manager no tiene una vacante en común con un
  empleado, *cuando* intenta consultar el perfil o el historial de ese empleado, *entonces* el
  sistema **deniega el acceso** y registra el intento en la bitácora.
- **Escenario 2 — Credenciales y transporte protegidos.** *Dado* que un usuario se registra o
  inicia sesión, *cuando* su contraseña se almacena, *entonces* se guarda con **hash** (nunca en
  texto plano), y todo el tráfico entre navegador y servidor viaja por **HTTPS**.
- **Escenario 3 — Sesión que expira.** *Dado* que un usuario deja su sesión inactiva por más de
  **30 minutos**, *cuando* vuelve a la aplicación, *entonces* el sistema la cierra y le exige
  iniciar sesión de nuevo antes de mostrar cualquier dato.

## Definición de Terminado

- [ ] Cumple los 3 escenarios, validados con pruebas que incluyen intentos no autorizados.
- [ ] La matriz de permisos por rol está documentada y cada endpoint la verifica.
- [ ] Certificado HTTPS válido en el ambiente de pruebas y hash de contraseñas confirmado.
- [ ] Ninguna respuesta del API expone la identidad del candidato seleccionado (RN-18).
- [ ] Validada por Jhon Mejia (Product Owner) y Juan Maldonado (QA Lead).

## Prioridad

**Alta (Crítica)**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Matriz de permisos por rol (Empleado, Manager, RR.HH., Líder Ejecutivo, Admin). | Product Owner / Configuration Manager |
| 2 | Autorización en cada endpoint verificando la relación entre solicitante y recurso. | Backend |
| 3 | Hash de contraseñas en el registro y en el cambio de contraseña. | Backend |
| 4 | Configurar HTTPS en el ambiente de pruebas. | DevOps Engineer |
| 5 | Expiración de sesión por 30 minutos de inactividad. | Backend |
| 6 | Registro en bitácora de los accesos denegados. | Backend |
| 7 | Pruebas de control de acceso para los escenarios "no autorizado". | QA Lead |

## Discusión

La confianza es el activo más frágil de TalentMatch: si un empleado sospecha que explorar otras
vacantes puede "filtrarse" a su Manager actual, dejará de usar la plataforma justo cuando más la
necesita. Esta historia es la base para que las demás (transparencia, resultado anónimo,
reporte ejecutivo) tengan sentido.

## Trabajo Relacionado

CU_02, CU_20, CU_21 · RF-02, RF-03, RF-31 · RNF-07, RNF-10 · RN-18

### Qué cambió frente a la v1.0

El Escenario 2 exigía **cifrado en reposo a nivel de base de datos**, que requiere configuración
de infraestructura fuera del alcance del MVP. Se reemplazó por **hash de contraseñas + HTTPS**,
que protege lo esencial y se implementa con el stack del curso. También se retiró la exigencia de
*"sin hallazgos críticos ni altos en una revisión tipo OWASP"* como criterio de cierre, y se
agregó el criterio verificable de que **ninguna respuesta exponga la identidad del seleccionado**
(RN-18).

---

# HU-007 — Avalar las habilidades de mi equipo

**Tipo:** Historia funcional · **Nueva en v2.0**

> **COMO** Manager / Líder Técnico,
> **QUIERO** revisar las habilidades que los empleados de mi equipo agregan a su perfil y marcarlas
> como avaladas o rechazarlas,
> **PARA** que el Puntaje Final se calcule sobre habilidades reales y no sobre autoevaluaciones
> infladas.

## Descripción

Cualquier empleado puede escribir "Kubernetes experto" en su perfil. Si eso alimentara
directamente el algoritmo, el ranking dejaría de ser confiable en una semana. Por eso toda
habilidad nace en estado **`agregado`** y solo yo, como su manager actual, puedo pasarla a
**`avalado`** (RN-19). Solo las avaladas entran al cálculo (RN-20).

## Criterios de Aceptación

- **Escenario 1 — Bandeja de habilidades por avalar.** *Dado* que un empleado de mi equipo agrega
  una habilidad, *cuando* abro mi bandeja, *entonces* la veo listada en estado `agregado` con el
  nivel que declaró y la fecha.
- **Escenario 2 — Avalar o rechazar.** *Dado* que reviso una habilidad, *cuando* la avalo,
  *entonces* pasa a estado `avalado` y queda disponible para el cálculo; *cuando* la rechazo,
  *entonces* permanece visible en el perfil como `agregado` pero **no entra al cálculo**.
- **Escenario 3 — El cálculo ignora lo no avalado.** *Dado* que un empleado tiene 5 habilidades
  agregadas y 2 avaladas, *cuando* se calcula su Match Score para una vacante, *entonces* solo
  las **2 avaladas** se consideran, y el desglose se lo explica así al empleado.

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] Prueba unitaria que demuestra que las habilidades `agregado` no alteran el Match Score.
- [ ] Solo el manager actual del empleado puede avalar sus habilidades (validado con prueba).
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta (Crítica)** — sin esta historia, HU-001 y HU-004 no tienen datos confiables.

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Modelo de datos de habilidad con estado `agregado` / `avalado`. | Configuration Manager |
| 2 | Endpoints de avalar y rechazar, restringidos al manager actual. | Backend |
| 3 | Filtrar por estado `avalado` en el cálculo del Match Score. | Backend |
| 4 | Bandeja de habilidades pendientes en el panel del Manager. | Frontend |
| 5 | Indicador de estado en el perfil del empleado. | Frontend |
| 6 | Pruebas del filtro de estado y del control de acceso al aval. | QA Lead |

## Trabajo Relacionado

CU_05, CU_06, CU_11 · RF-06, RF-07, RF-08 · RN-19, RN-20

---

# HU-008 — Postularme a varias vacantes con reglas claras

**Tipo:** Historia funcional · **Nueva en v2.0**

> **COMO** Empleado,
> **QUIERO** postularme en cualquier momento a todas las vacantes que me interesen, sabiendo de
> antemano si cumplo los requisitos de antigüedad y de espera,
> **PARA** no perder oportunidades ni recibir un rechazo administrativo por una regla que
> desconocía.

## Descripción

El modelo es **continuo**: no hay fechas límite ni convocatorias (RN-01, RN-06). Puedo tener
varias postulaciones activas a la vez (RN-08). Pero hay dos reglas que debo conocer antes de
hacer clic: debo llevar **6 meses en mi rol actual** (RN-07), y si ya me rechazaron en un puesto,
debo esperar **6 meses y que se abra una nueva rotación** en él (RN-10). Y si acepto una vacante,
mis demás postulaciones se cancelan solas (RN-09).

## Criterios de Aceptación

- **Escenario 1 — Antigüedad insuficiente.** *Dado* que llevo 4 meses en mi rol actual, *cuando*
  abro una vacante, *entonces* el botón de postularme aparece deshabilitado con el mensaje
  *"Podrás postularte a partir del [fecha]: se requieren 6 meses en el rol actual"*.
- **Escenario 2 — Postulaciones simultáneas y aviso de puntaje bajo.** *Dado* que ya tengo 2
  postulaciones activas, *cuando* me postulo a una tercera con Puntaje Final de 58 %, *entonces*
  el sistema **me permite postularme** pero me advierte que mis posibilidades son bajas porque
  estoy por debajo del umbral de 65 % (RN-28).
- **Escenario 3 — Cancelación en cascada.** *Dado* que tengo 3 postulaciones activas, *cuando*
  acepto una de ellas, *entonces* las otras 2 se cancelan automáticamente, aparecen como
  *"cancelada por rotación aceptada"* en mi historial, y **los managers de esas vacantes no
  reciben notificación** (RN-09).

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] Las tres reglas (6 meses de rol, 6 meses de bloqueo, cancelación en cascada) tienen prueba unitaria.
- [ ] Ningún mensaje de bloqueo deja al usuario sin saber **cuándo** podrá postularse.
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Validación de antigüedad ≥ 6 meses en el rol actual. | Backend |
| 2 | Validación del bloqueo de 6 meses + nueva rotación en el puesto. | Backend |
| 3 | Cancelación automática y silenciosa de las demás postulaciones activas. | Backend |
| 4 | Mensajes de bloqueo con la fecha exacta de habilitación. | Frontend |
| 5 | Aviso de "posibilidades bajas" sin bloquear la postulación. | Frontend |
| 6 | Pruebas de las tres reglas y del caso de cancelación en cascada. | QA Lead |

## Trabajo Relacionado

CU_12, CU_17 · RF-17, RF-19, RF-20, RF-26 · RN-06…RN-10, RN-28

---

# HU-009 — Que la vacante se abra sola cuando alguien deja su puesto

**Tipo:** Historia funcional · **Nueva en v2.0**

> **COMO** Analista de RR.HH.,
> **QUIERO** que el sistema genere la vacante automáticamente cuando un empleado deja su
> asignación, con una ventana de 24 horas de revisión si el origen fue una rotación,
> **PARA** que ningún puesto quede sin cubrir por olvido y para que la cadena de rotaciones no se
> detenga.

## Descripción

Cuando alguien rota, renuncia o su proyecto cierra, su puesto queda libre (RN-02). Si nadie crea
la vacante a mano, el puesto se pierde de vista. El sistema debe crearla solo. Y cuando el origen
es una **rotación aceptada**, la vacante se activa **24 horas después** para que el encargado de
supervisar los cambios alcance a revisarla (RN-03). Esto genera **cadenas**: A deja su puesto,
B lo toma dejando el suyo, C toma el de B. Cada eslabón es una vacante independiente (RN-04).

## Criterios de Aceptación

- **Escenario 1 — Vacante inmediata.** *Dado* que un empleado renuncia o su proyecto se cierra,
  *cuando* se libera su `AsignacionProyecto`, *entonces* el sistema crea la Vacante y la activa
  **de inmediato**.
- **Escenario 2 — Activación diferida 24 h.** *Dado* que la vacante nace de una **rotación
  aceptada**, *cuando* se crea, *entonces* queda en estado `pendiente de activación` y pasa a
  `activa` **24 horas después**, sin aparecer antes en las búsquedas de los empleados.
- **Escenario 3 — Alerta por vacante estancada.** *Dado* que una vacante lleva más días activos
  que el umbral configurado sin ningún Match, *cuando* se cumple ese plazo, *entonces* el sistema
  notifica al **manager solicitante** y al **Jefe del área** (RN-05).

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] La ventana de 24 h y el umbral de días son **configurables** sin recompilar (RNF-12).
- [ ] Una cadena de 3 rotaciones genera 3 vacantes independientes, verificado en pruebas.
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Alta**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Disparador de creación de vacante al liberarse una `AsignacionProyecto`. | Backend |
| 2 | Estado `pendiente de activación` y job que activa a las 24 h. | Backend / DevOps Engineer |
| 3 | Job de revisión de vacantes estancadas y notificación al manager y al Jefe de área. | Backend |
| 4 | Parámetros (24 h, umbral de días) en el archivo de configuración. | Configuration Manager |
| 5 | Panel de vacantes por estado para RR.HH. | Frontend |
| 6 | Pruebas de la cadena de rotaciones y de la activación diferida. | QA Lead |

## Trabajo Relacionado

CU_18, CU_19 · RF-12, RF-14 · RNF-12 · RN-02, RN-03, RN-04, RN-05, RN-30 · Umbral: **10 días** (PEN-01 confirmado)

---

# HU-010 — Ver el reporte de los 10 empleados más destacados

**Tipo:** Historia funcional · **Nueva en v2.0**

> **COMO** Líder Ejecutivo,
> **QUIERO** un reporte con los 10 empleados con más habilidades avaladas, más rotaciones y más
> años en la empresa,
> **PARA** tener un insumo objetivo en las conversaciones de ascenso.

## Descripción

Las decisiones de ascenso suelen apoyarse en quién es más visible. El reporte top 10 (RN-21) da
un punto de partida objetivo: quién realmente amplió sus habilidades, quién se movió por la
empresa y quién lleva más tiempo aportando. **La decisión final sigue siendo humana y fuera de
la app.** Y como cada empleado sabe que esto existe (RN-22), rotar y desarrollarse deja de ser
un riesgo de carrera para volverse un incentivo.

## Criterios de Aceptación

- **Escenario 1 — Ranking top 10.** *Dado* que abro el reporte ejecutivo, *entonces* veo los 10
  primeros empleados ordenados por habilidades **avaladas**, número de rotaciones y años en la
  empresa, con el valor de cada criterio visible.
- **Escenario 2 — Aviso de incentivo al empleado.** *Dado* que un empleado confirma una rotación,
  *cuando* recibe la confirmación, *entonces* el mensaje le informa que sus rotaciones y mejoras
  de habilidades **aumentan su probabilidad** de aparecer en el reporte ejecutivo (RN-22).
- **Escenario 3 — Solo insumo, no decisión.** *Dado* que estoy viendo el reporte, *entonces* la
  pantalla indica explícitamente que es un **insumo de apoyo** y que la app no ejecuta ni registra
  decisiones de ascenso.

## Definición de Terminado

- [ ] Cumple los 3 criterios de aceptación.
- [ ] El reporte solo cuenta habilidades en estado `avalado` (RN-20).
- [ ] Solo los roles Líder Ejecutivo y RR.HH. pueden abrirlo (RNF-07).
- [ ] Validada por Jhon Mejia (Product Owner).

## Prioridad

**Media**

## Subtareas Técnicas Derivadas

| # | Subtarea | Responsable |
|---|---|---|
| 1 | Consulta del top 10 por habilidades avaladas, rotaciones y antigüedad. | Backend |
| 2 | Pantalla del reporte con los tres criterios visibles. | Frontend |
| 3 | Mensaje de incentivo en la confirmación de rotación. | Frontend |
| 4 | Restricción de acceso al rol Líder Ejecutivo y RR.HH. | Backend |
| 5 | Pruebas del ordenamiento y del control de acceso. | QA Lead |

## Trabajo Relacionado

CU_15, CU_22 · RF-28, RF-29 · RNF-07 · RN-21, RN-22

---

## Resumen de trazabilidad

| HU | Título | Tipo | Prioridad | RN | RF | CU |
|---|---|---|---|---|---|---|
| HU-001 | Revisar el ranking de candidatos | Funcional | Alta (Crítica) | RN-12…RN-14, RN-27 | RF-15, RF-21, RF-22 | CU_11, CU_14 |
| HU-002 | Detectar patrones de rechazo sesgados | Funcional | Media | RN-29 | RF-32 | CU_24 |
| HU-003 | Perfil anónimo del seleccionado | Funcional | Alta | RN-10, RN-17, RN-18 | RF-27 | CU_16, CU_21 |
| HU-004 | Competir por méritos | Funcional | Alta (Crítica) | RN-13, RN-14, RN-18, RN-29 | RF-15, RF-21, RF-27, RF-32 | CU_14…CU_16, CU_21, CU_24 |
| HU-005 | Tiempo de respuesta | No funcional | Media | RN-01 | — | — |
| HU-006 | Protección de datos sensibles | No funcional | Alta (Crítica) | RN-18 | RF-02, RF-03 | CU_02, CU_20, CU_21 |
| HU-007 | Avalar habilidades del equipo | Funcional | Alta (Crítica) | RN-19, RN-20 | RF-06…RF-08 | CU_05, CU_06, CU_11 |
| HU-008 | Postularme con reglas claras | Funcional | Alta | RN-06…RN-10, RN-28 | RF-17, RF-19, RF-20, RF-26 | CU_12, CU_17 |
| HU-009 | Vacante automática al liberar un puesto | Funcional | Alta | RN-02…RN-05, RN-30 | RF-12, RF-14 | CU_18, CU_19 |
| HU-010 | Reporte ejecutivo top 10 | Funcional | Media | RN-21, RN-22 | RF-28, RF-29 | CU_15, CU_22 |

**Fuente del contexto:** [Reglas de Negocio v2.0](../reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md)
y wiki del repositorio TalentMatch (Home, Project Overview, Problem & Solution, User Roles & Flows,
Matching Algorithm, StakeHolders).
