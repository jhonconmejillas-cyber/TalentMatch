# TalentMatch — Especificación de Requerimientos

| Campo | Valor |
|---|---|
| **Documento** | Requerimientos Funcionales (RF) y No Funcionales (RNF) |
| **Versión** | 2.1 — corregida contra las [Reglas de Negocio v2.1](../reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) |
| **Fecha** | 2026-09-21 |
| **Sistema** | TalentMatch — Movilidad Interna de Ingenieros |
| **Total RF** | 33 numerados, **32 activos** (RF-05 eliminado, ver PEN-03) |
| **Total RNF** | 13 |

---

## Resumen ejecutivo

Este documento especifica los requerimientos funcionales y no funcionales de **TalentMatch**,
la plataforma de **movilidad interna** que conecta ingenieros con vacantes dentro de su propia
empresa usando un algoritmo **sin IA**, explicable y auditable.

La versión 1.0 de este documento describía parcialmente un proceso de **selección externa**
(feedback obligatorio del manager, apelaciones, métricas de desempeño individual). La
versión 2.0 **realinea todos los requerimientos** con las Reglas de Negocio vigentes:
modelo de rotación continuo, resultado de rechazo anónimo y automático, y cálculo basado
exclusivamente en **habilidades avaladas** y **senioridad**.

---

## 1. Requerimientos Funcionales

Describen **qué debe hacer** el sistema. La columna **RN** indica la regla de negocio que
los origina; la columna **CU** el caso de uso que los realiza.

### 1.1 Acceso y cuentas

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-01** | Registrarse | El sistema debe permitir a un usuario crear una cuenta con nombre completo, correo electrónico corporativo y contraseña, validando los datos antes de almacenarlos. | Usuario | — | CU_01 | Alta |
| **RF-02** | Iniciar sesión | El sistema debe autenticar al usuario con correo y contraseña, y cargar el perfil correspondiente a su rol. | Usuario registrado | — | CU_02 | Alta |
| **RF-03** | Gestionar usuarios y roles | El sistema debe permitir crear, desactivar y asignar rol (Empleado, Manager, RR.HH., Líder Ejecutivo, Administrador) a las cuentas. | Administrador del Sistema | RN-§11 | CU_23 | Alta |

### 1.2 Perfil del empleado

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-04** | Gestionar perfil profesional | El sistema debe permitir al empleado registrar y actualizar su senioridad, años de experiencia, área y proyecto actual. | Empleado | RN-23 | CU_03 | Alta |
| **RF-05** | ~~Definir meta de carrera~~ | **Eliminado.** El Career Impact Score y la meta de carrera se sacaron del alcance del MVP (PEN-03). | — | — | — | — |
| **RF-06** | Agregar habilidades autoreportadas | El sistema debe permitir al empleado agregar habilidades a su perfil, guardándolas siempre en estado `agregado`. | Empleado | RN-19 | CU_05 | Alta |
| **RF-07** | Avalar habilidades | El sistema debe permitir al manager actual revisar las habilidades `agregado` de su equipo y cambiarlas a `avalado` o rechazarlas. | Manager / Líder Técnico | RN-19 | CU_06 | Alta |
| **RF-08** | Excluir habilidades no avaladas del cálculo | El sistema debe usar **únicamente** habilidades en estado `avalado` al calcular el Match Score. | Sistema | RN-20 | CU_11 | Alta |
| **RF-09** | Gestionar lista de deseos (wishlist) | El sistema debe permitir al empleado guardar habilidades que desea aprender. | Empleado | — | CU_07 | Media |
| **RF-10** | Notificar vacantes por wishlist | El sistema debe notificar proactivamente al empleado cuando se activa una vacante que cubre habilidades de su wishlist. | Sistema | RN-01 | CU_08 | Media |

### 1.3 Vacantes

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-11** | Publicar vacante interna | El sistema debe permitir al manager publicar una vacante indicando habilidades críticas, deseables, nivel de senioridad y área. | Manager / Líder Técnico | RN-01 | CU_09 | Alta |
| **RF-12** | Generar vacante automáticamente | El sistema debe crear una Vacante automáticamente cuando un empleado deja una `AsignacionProyecto`. Si el origen es una rotación aceptada, la vacante se activa **24 horas después**. | Sistema | RN-02, RN-03 | CU_18 | Alta |
| **RF-13** | Buscar y filtrar vacantes | El sistema debe permitir al empleado buscar vacantes activas y filtrarlas por título, habilidades, área y senioridad, **incluyendo áreas distintas a la suya**. | Empleado | RN-11 | CU_10 | Alta |
| **RF-14** | Alertar vacante sin match | El sistema debe notificar al manager solicitante y al Jefe del área cuando una vacante supere el umbral de días sin Match. | Sistema | RN-05, RN-30 | CU_19 | Media |

### 1.4 Matching

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-15** | Calcular Puntaje Final | El sistema debe calcular el Match Score y el `Puntaje Final = Match Score` para cada par empleado–vacante, usando reglas lógicas explicables y **sin IA**. | Sistema | RN-13, RN-23, RN-25 | CU_11 | Alta |
| **RF-16** | Mostrar desglose al empleado | El sistema debe mostrar al empleado su Puntaje Final y el desglose del Match Score **antes** de que decida postularse. | Empleado | RN-29 (§10) | CU_11 | Alta |
| **RF-17** | Advertir posibilidades bajas | Si el Puntaje Final es **< 65 %**, el sistema debe informar al empleado que sus posibilidades son bajas, **sin bloquear** su postulación. | Sistema | RN-28 | CU_12 | Alta |
| **RF-18** | Calcular Costo de Rotación | El sistema debe estimar y mostrar al manager, de forma **puramente informativa**, el costo de perder al empleado frente al costo de moverlo internamente. **No altera el Puntaje Final ni el ranking** (PEN-02). | Sistema | RN-26 | CU_13 | Media |

### 1.5 Postulación y decisión

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-19** | Postularse a una vacante | El sistema debe permitir al empleado postularse en cualquier momento, validando que lleve **≥ 6 meses** en su rol actual y que no esté dentro del bloqueo de **6 meses** por rechazo previo en ese puesto. | Empleado | RN-06, RN-07, RN-10 | CU_12 | Alta |
| **RF-20** | Permitir postulaciones simultáneas | El sistema debe permitir que un empleado mantenga varias postulaciones activas al mismo tiempo. | Empleado | RN-08 | CU_12 | Alta |
| **RF-21** | Mostrar ranking inmutable de candidatos | El sistema debe mostrar al manager únicamente los candidatos con Puntaje Final **≥ 65 %**, ordenados de mayor a menor, **sin permitir reordenamiento manual**. | Manager / Líder Técnico | RN-14, RN-27 | CU_14 | Alta |
| **RF-22** | Agrupar peticiones por ciclo de 3 días | El sistema debe acumular las peticiones y habilitar la resolución del Match al manager **cada 3 días**. | Sistema | RN-12 | CU_14 | Media |
| **RF-23** | Aceptar o rechazar candidato | El sistema debe permitir al manager aceptar o rechazar a cada candidato del ranking, sin exigirle redactar justificación alguna. | Manager / Líder Técnico | RN-13, RN-17 | CU_15, CU_16 | Alta |
| **RF-24** | Ejecutar transición automática | Al aceptarse un Match, el sistema debe cambiar la `AsignacionProyecto` del empleado **automáticamente**, sin aprobación manual de su manager actual. | Sistema | RN-15 | CU_15 | Alta |
| **RF-25** | Notificar únicamente a RR.HH. | Al aceptarse una postulación, el sistema debe notificar **solo a RR.HH.**; ningún otro rol recibe notificación automática del cambio. | Sistema | RN-16 | CU_15 | Alta |
| **RF-26** | Cancelar postulaciones restantes | Cuando un empleado acepta una vacante, el sistema debe cancelar automáticamente sus demás postulaciones activas **sin notificar** a los managers afectados. | Sistema | RN-09 | CU_17 | Alta |

### 1.6 Resultado del rechazo y reconocimiento

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-27** | Notificar resultado con perfil anónimo | Al cerrarse una vacante, el sistema debe mostrar a cada candidato rechazado el **Puntaje Final** y las **habilidades** de la persona seleccionada, **sin nombre ni dato identificable**. | Sistema | RN-17, RN-18 | CU_16 | Alta |
| **RF-28** | Informar el incentivo del reporte ejecutivo | En cada match confirmado, el sistema debe informar al empleado que sus rotaciones y habilidades aumentan su probabilidad de aparecer en el reporte ejecutivo top 10. | Sistema | RN-22 | CU_15 | Media |
| **RF-29** | Generar reporte ejecutivo top 10 | El sistema debe generar un ranking de los 10 empleados con más habilidades avaladas, más rotaciones y más años en la empresa. | Líder Ejecutivo | RN-21 | CU_22 | Media |

### 1.7 Historial, control y administración

| ID | Nombre | Descripción | Actor | RN | CU | Prioridad |
|---|---|---|---|---|---|---|
| **RF-30** | Consultar historial de proyectos y rotaciones | El sistema debe permitir consultar el historial de proyectos y rotaciones de un empleado a quien tenga relación autorizada con él. | Manager, Analista RR.HH. | RN-21 | CU_20 | Media |
| **RF-31** | Consultar historial propio de postulaciones | El sistema debe permitir al empleado ver sus postulaciones, su posición en cada ranking y el resultado anónimo de cada una. **Solo el propio empleado y RR.HH. pueden acceder a su historial de rechazos.** | Empleado | RN-18 | CU_21 | Media |
| **RF-32** | Alertar rechazos repetidos | El sistema debe emitir una alerta a RR.HH. cuando un mismo manager rechace **2 o más** candidatos viables (≥ 65 %). | Sistema | RN-29 | CU_24 | Alta |
| **RF-33** | Configurar umbrales | El sistema debe permitir a RR.HH. ajustar el umbral de viabilidad (65 %) y el umbral de días sin match (10 días) **sin recompilar**. | Analista RR.HH. | RN-27 | CU_25 | Media |

---

## 2. Requerimientos No Funcionales

> **Nota sobre la simplificación (v2.0).** La versión 1.0 marcaba los 9 RNF como prioridad
> *Alta* y fijaba metas difíciles de alcanzar y de verificar en un MVP académico de 16 semanas
> (500 usuarios concurrentes, 99 % de uptime, cifrado en reposo, "mecanismos verificables de
> equidad"). En la v2.0 cada uno se **reescribió a una meta medible, alcanzable y comprobable
> con las herramientas del curso**, y se repriorizó de forma realista. La §2.2 muestra el
> antes y después.

### 2.1 Catálogo

| ID | Nombre | Descripción | Categoría | Prioridad |
|---|---|---|---|---|
| **RNF-01** | Usuarios concurrentes | La plataforma debe soportar **50 usuarios concurrentes** sin que el tiempo de respuesta supere los límites de RNF-04. | Escalabilidad | Media |
| **RNF-02** | Disponibilidad en horario laboral | La plataforma debe estar disponible de **lunes a viernes, 7:00–19:00**, con una disponibilidad mensual **≥ 95 %** en ese horario. Las tareas de mantenimiento se programan fuera de él. | Disponibilidad | Media |
| **RNF-03** | Baja carga cognitiva | Cualquier acción principal (ver vacantes, postularse, ver ranking, aceptar/rechazar) debe alcanzarse en **máximo 3 clics** desde el panel del usuario. | Usabilidad | Alta |
| **RNF-04** | Tiempo de respuesta | El **90 %** de las páginas y consultas debe responder en **menos de 3 segundos**; el ranking de una vacante con hasta **20 candidatos** debe cargar en menos de **2 segundos**. | Rendimiento | Alta |
| **RNF-05** | Diseño sencillo por defecto | La interfaz debe mostrar solo las opciones esenciales; las opciones avanzadas (filtros extra, desgloses detallados) deben estar disponibles pero **ocultas tras un control explícito**. | Usabilidad / Diseño | Media |
| **RNF-06** | Bitácora del cálculo | Cada cálculo de Puntaje Final debe **guardarse en una tabla de bitácora** con: empleado, vacante, Match Score, Puntaje Final, umbral usado, y fecha/hora. La bitácora es de solo lectura. | Trazabilidad | Alta |
| **RNF-07** | Autenticación y permisos por rol | El acceso debe requerir usuario y contraseña, las contraseñas deben almacenarse con **hash** (nunca en texto plano), el tráfico debe viajar por **HTTPS**, y cada pantalla debe validar el rol del solicitante contra la **matriz de permisos** documentada. | Seguridad | Alta |
| **RNF-08** | Inmediatez de las notificaciones | Toda notificación generada por el sistema (resultado de vacante, alerta a RR.HH., aviso de wishlist) debe quedar disponible para su destinatario en **menos de 5 minutos**. No depende de ninguna acción humana. | Rendimiento de proceso | Media |
| **RNF-09** | Neutralidad del cálculo | El cálculo del Puntaje Final debe usar **exclusivamente** habilidades avaladas y senioridad. **Ningún dato personal** (nombre, edad, género, foto, área, antigüedad con el manager) puede intervenir, y el ranking resultante **no debe poder reordenarse manualmente**. Verificable con pruebas unitarias. | Equidad | Alta |
| **RNF-10** | Privacidad del historial de rechazos | El historial de rechazos de un empleado solo debe ser accesible para **ese empleado y para RR.HH.**; cualquier otro intento debe denegarse y registrarse en la bitácora. | Privacidad | Alta |
| **RNF-11** | Compatibilidad | La aplicación web debe funcionar en **Chrome y Edge** (dos últimas versiones estables) a partir de una resolución de **1366×768**. | Portabilidad | Media |
| **RNF-12** | Configurabilidad sin recompilar | El umbral de viabilidad (65 %), el umbral de días sin match (10 días), el ciclo de evaluación (3 días) y los plazos (6 meses, 24 horas) deben residir en un **archivo de configuración** editable sin recompilar ni redesplegar. | Mantenibilidad | Media |
| **RNF-13** | Idioma y mensajes de error | Toda la interfaz y todos los mensajes de error deben estar en **español**, indicando el campo afectado y cómo corregirlo. | Usabilidad | Baja |

### 2.2 Antes y después de la simplificación

| RNF v1.0 | Meta original (compleja) | Meta v2.0 (simplificada) | Por qué |
|---|---|---|---|
| RNF-01 | 500 usuarios concurrentes | **50** usuarios concurrentes (RNF-01) | 500 exige infraestructura y pruebas de carga fuera del alcance de un MVP de 16 semanas. 50 cubre el piloto y se prueba con herramientas libres. |
| RNF-02 | Uptime ≥ 99 % (≈ 7 h de caída al año) | **≥ 95 % en horario laboral L-V 7–19** (RNF-02) | 99 % obliga a redundancia y monitoreo 24/7. La plataforma solo se usa en horario de oficina. |
| RNF-03 | Máximo 3 clics | **Se mantiene** (RNF-03) | Ya era simple y verificable. |
| RNF-04 | "Priorizar rendimiento sobre estética" | **90 % de las páginas < 3 s; ranking < 2 s con 20 candidatos** (RNF-04) | La versión original no era medible ni verificable; ahora tiene un número contra el cual probar. |
| RNF-05 | Progressive disclosure | **Opciones avanzadas ocultas tras un control explícito** (RNF-05) | Misma intención, redactada como algo revisable en una lista de chequeo de UI. |
| RNF-06 | "Toda decisión del algoritmo auditable" | **Una tabla de bitácora con el Match Score, el Puntaje Final, el umbral y la fecha** (RNF-06) | "Auditable" era ambiguo; ahora es una tabla concreta que se implementa y se consulta. |
| RNF-07 | Control de acceso granular + cifrado en reposo | **Login + hash de contraseñas + HTTPS + matriz de permisos por rol** (RNF-07) | El cifrado a nivel de base de datos exige configuración de infraestructura. Hash + HTTPS + roles logra la protección esencial con el stack del curso. |
| RNF-08 | SLA de feedback de rechazo ≤ 24 h | **Notificaciones del sistema disponibles en < 5 min** (RNF-08) | El feedback humano **ya no existe** (RN-17). Lo que queda es una notificación automática, que es más rápida y más fácil de medir. |
| RNF-09 | "Mecanismos verificables de equidad" | **Lista cerrada de campos permitidos en el cálculo + ranking no reordenable** (RNF-09) | Convierte un objetivo abstracto en dos pruebas unitarias concretas. |
| — | *(nuevo)* | RNF-10 Privacidad del historial de rechazos | Estaba como RF-13; pertenece a los atributos de calidad. |
| — | *(nuevo)* | RNF-11, RNF-12, RNF-13 | Atributos básicos (compatibilidad, configurabilidad, idioma) que faltaban y son fáciles de cumplir. |

---

## 3. Cambios de la versión 1.0 a la 2.0

### 3.1 Requerimientos eliminados

| RF v1.0 | Motivo de la eliminación |
|---|---|
| **RF-06** Mostrar métricas de velocidad histórica | El algoritmo (RN-23, RN-24) **no usa** métricas de desempeño individual. Incluirlas contradice RNF-09 (neutralidad del cálculo) y reintroduce el sesgo que el producto busca eliminar. |
| **RF-07** Mostrar historial de reporte de bloqueos | Mismo motivo que RF-06: es un dato de desempeño que no entra al Puntaje Final y expone información sensible del empleado al manager. |
| **RF-08** Registrar indicador de actitud ante feedback | El concepto de *feedback del manager* **desaparece** por RN-17. Se reemplaza por **RF-07 Avalar habilidades**, que es el mecanismo real de validación del manager sobre su equipo (RN-19). |
| **RF-10** Permitir apelar el resultado de selección | Las Reglas de Negocio **no contemplan apelación**. El rechazo es anónimo y automático (RN-17, RN-18): no hay decisión argumentada que apelar. El mecanismo de control equivalente es la **alerta automática a RR.HH. por rechazos repetidos** (RN-29 → RF-32). |

### 3.2 Requerimientos reformulados

| RF v1.0 | RF v2.0 | Qué cambió |
|---|---|---|
| RF-01 Registrar Vacante Interna | **RF-11** Publicar vacante interna | El actor pasa de *Analista RR.HH.* a **Manager / Líder Técnico**: es el manager solicitante quien publica (RN-05, RN-12). |
| RF-03 Postularse a Vacante | **RF-19** Postularse a una vacante | Se agregan las validaciones de **6 meses de antigüedad** en el rol (RN-07) y de **bloqueo de 6 meses** por rechazo previo (RN-10). |
| RF-05 Mostrar perfil de habilidades | **RF-06 / RF-07 / RF-08** | Se introduce el ciclo de estados `agregado` → `avalado` (RN-19) y la exclusión de las no avaladas del cálculo (RN-20). |
| RF-09 Calcular % de compatibilidad | **RF-15 / RF-16** | Se explicita la fórmula completa de las tres métricas y la obligación de mostrar el desglose al empleado. |
| RF-11 Notificar feedback de rechazo | **RF-27** Notificar resultado con perfil anónimo | **Cambio de fondo:** ya no hay feedback del manager. El sistema entrega el % y las habilidades del seleccionado, sin nombre (RN-17, RN-18). |
| RF-12 Ejecutar auditorías periódicas de sesgos | **RF-32** Alertar rechazos repetidos | Se concreta en la regla real: alerta a RR.HH. ante 2+ rechazos de candidatos viables por el mismo manager (RN-29). |
| RF-13 Restringir acceso a historial de rechazos | **RNF-10** | Se reclasifica como atributo de calidad (privacidad) y se refleja también en RF-31. |

### 3.3 Requerimientos nuevos

`RF-01`, `RF-02`, `RF-03` (acceso y cuentas) ·
`RF-07`, `RF-08` (aval de habilidades) · `RF-09`, `RF-10` (wishlist y notificación proactiva) ·
`RF-12` (generación automática de vacantes) · `RF-14` (vacante sin match) ·
`RF-17` (aviso de posibilidades bajas) · `RF-18` (costo de rotación) ·
`RF-20` (postulaciones simultáneas) · `RF-22` (ciclo de 3 días) ·
`RF-24`, `RF-25`, `RF-26` (transición automática, notificación a RR.HH., cancelación en cascada) ·
`RF-28`, `RF-29` (incentivo y reporte ejecutivo) · `RF-33` (configuración de umbrales).

> `RF-05` (meta de carrera) se propuso en la v2.0 y se **eliminó** en la v2.1 al sacar el
> Career Impact Score del alcance del MVP (PEN-03).

---

## 4. Dependencias críticas

```
RF-06 (agregar habilidad)
      |
      v
RF-07 (avalar) --> RF-08 (solo avaladas cuentan)
                          |
RF-04 (perfil) -----------+--> RF-15 (calcular Puntaje Final)  <-- CORAZON DEL SISTEMA
RF-11/RF-12 (vacante) ----+          |
                                     +--> RF-16 (desglose al empleado)
                                     +--> RF-17 (aviso < 65%)
                                     +--> RF-21 (ranking inmutable al manager)
                                              |
                                              +--> RF-23 (aceptar / rechazar)
                                                       |
                                    ACEPTA <-----------+-----------> RECHAZA
                                       |                                |
                    RF-24 transicion automatica              RF-27 perfil anonimo
                    RF-25 notifica solo a RRHH               RF-32 alerta si 2+ rechazos
                    RF-26 cancela otras postulaciones
                    RF-28 aviso de incentivo
                    RF-12 genera la vacante del puesto liberado (+24h)
```

`RF-15` es el corazón del sistema. `RNF-06` (bitácora) y `RNF-09` (neutralidad) son las dos
garantías de calidad que lo hacen auditable, y deben validarse **antes de pasar a producción**.

---

## 5. Matriz de trazabilidad resumida

| Bloque | RN | RF | CU |
|---|---|---|---|
| Acceso y cuentas | §11 | RF-01…RF-03 | CU_01, CU_02, CU_23 |
| Perfil y habilidades | RN-19, RN-20, RN-23 | RF-04, RF-06…RF-10 | CU_03, CU_05…CU_08 |
| Vacantes | RN-01…RN-05, RN-11, RN-30 | RF-11…RF-14 | CU_09, CU_10, CU_18, CU_19 |
| Matching | RN-13, RN-23…RN-28 | RF-15…RF-18 | CU_11, CU_12, CU_13 |
| Postulación y decisión | RN-06…RN-16 | RF-19…RF-26 | CU_12, CU_14…CU_17 |
| Rechazo y reconocimiento | RN-17, RN-18, RN-21, RN-22 | RF-27…RF-29 | CU_15, CU_16, CU_22 |
| Historial y control | RN-18, RN-21, RN-29 | RF-30…RF-33 | CU_20, CU_21, CU_24, CU_25 |

La trazabilidad completa **CU → RF → RN** se detalla en el
[documento de Casos de Uso](../casos-uso/CU-TalentMatch.md#matriz-de-trazabilidad).
