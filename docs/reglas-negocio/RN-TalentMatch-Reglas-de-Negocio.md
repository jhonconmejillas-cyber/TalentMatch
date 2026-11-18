# TalentMatch — Reglas de Negocio

| Campo | Valor |
|---|---|
| **Documento** | Reglas de Negocio (RN) |
| **Versión** | 2.1 — PEN-01, PEN-02 y PEN-03 confirmados |
| **Fecha** | 2026-09-21 |
| **Estado** | Vigente — fuente única de verdad |
| **Alcance** | MVP (4 meses / 16 semanas) |

> Este documento es la **fuente única de verdad** del proyecto. Las historias de usuario
> ([HU](../historias-usuario/HU-TalentMatch.md)), los requerimientos
> ([RF/RNF](../requerimientos/RF-RNF-TalentMatch.md)), los casos de uso
> ([CU](../casos-uso/CU-TalentMatch.md)) y el wiki deben ser consistentes con él.
> Ante cualquier contradicción, **prevalece esta página**.

---

## 1. Modelo de rotación

| ID | Regla |
|---|---|
| **RN-01** | TalentMatch opera con un **modelo continuo**: no existen fechas ni ventanas fijas de rotación, ni periodos de publicación o de selección predefinidos. Es consistente con los marketplaces internos de talento del mercado (Schneider Electric, Unilever FLEX). |
| **RN-02** | Una **Vacante** se genera **automáticamente** cuando un Empleado deja una `AsignacionProyecto`, sea por renuncia, por rotación aceptada, o por cierre/creación de un proyecto. |
| **RN-03** | Si la Vacante nace de una **rotación aceptada**, se activa **24 horas después** de la aceptación, para dar tiempo al encargado de supervisar los cambios a revisarla. |
| **RN-04** | Una rotación puede generar una **cadena de vacantes** (A deja su puesto → B lo toma dejando el suyo → C lo toma…). Cada eslabón se trata como una **Vacante independiente**; el sistema **no modela la cadena** como entidad. |
| **RN-05** | Si una Vacante permanece **sin Match** más allá de **10 días**, se notifica al **manager solicitante** y al **Jefe del área**. La decisión de contratar externamente o eliminar el puesto queda **fuera del alcance** de la aplicación. |

---

## 2. Postulación

| ID | Regla |
|---|---|
| **RN-06** | Las **Peticiones** (postulaciones) se reciben de forma **continua**; no existe fecha límite de postulación. |
| **RN-07** | Un empleado debe llevar **mínimo 6 meses** en su rol actual para poder postularse a una vacante. |
| **RN-08** | Un empleado **puede postularse a varias vacantes a la vez**. |
| **RN-09** | Si el empleado **acepta** una vacante, sus **demás postulaciones activas se cancelan automáticamente**. El manager de esas otras vacantes **no recibe notificación**: el candidato simplemente desaparece de su lista. |
| **RN-10** | Un empleado **no puede repostularse** a una vacante en la que fue rechazado, salvo que **pasen 6 meses** **y** se abra una **nueva rotación en ese mismo puesto**. |
| **RN-11** | Los empleados pueden rotar entre proyectos de **diferentes áreas**, no solo dentro de su área actual. |

---

## 3. Evaluación y matching

| ID | Regla |
|---|---|
| **RN-12** | El manager solicitante evalúa las peticiones acumuladas **cada 3 días** antes de resolver el Match (aceptación / rechazo). |
| **RN-13** | Cada Vacante se resuelve **por mérito** usando `Puntaje Final = Match Score`. |
| **RN-14** | El **ranking es inmutable**: ni el manager ni ningún otro rol puede reordenarlo manualmente. |
| **RN-15** | El cambio de `AsignacionProyecto` al aceptarse un Match es **automático** en el sistema, **sin aprobación manual** del manager actual del empleado. |
| **RN-16** | Al aceptarse una postulación, **solo RR.HH. es notificado por la aplicación**. RR.HH. revisa la aceptación y avisa a las partes involucradas; la **comunicación humana** del cambio la gestiona la empresa, no la app. |

---

## 4. Resultado del rechazo

| ID | Regla |
|---|---|
| **RN-17** | El empleado rechazado **NO recibe feedback del manager**. El manager **no redacta** ningún texto de retroalimentación y **nada bloquea** el cierre de la vacante. |
| **RN-18** | En su lugar, el sistema muestra automáticamente al empleado rechazado el **porcentaje (Puntaje Final)** y las **habilidades/conocimientos** de la persona que sí fue seleccionada, **sin mostrar su nombre ni ningún dato identificable**. |

> ⚠️ **Cambio respecto a versiones anteriores del wiki y de los documentos del taller.**
> Las versiones previas hablaban de "feedback obligatorio" del manager y de un bloqueo del
> cierre de la vacante. Esa regla **queda derogada** y reemplazada por RN-17 y RN-18.

---

## 5. Habilidades

| ID | Regla |
|---|---|
| **RN-19** | Toda habilidad que el empleado agrega a su perfil queda en estado **`agregado`** (autoreportada) hasta que **su manager actual** la revise y la cambie a **`avalado`**. |
| **RN-20** | **Solo las habilidades `avalado`** se incluyen en el cálculo del Puntaje Final. Las habilidades `agregado` sin avalar **no cuentan**, aunque sí son visibles en el perfil. |

---

## 6. Reporte ejecutivo y reconocimiento

| ID | Regla |
|---|---|
| **RN-21** | La aplicación genera para los **líderes ejecutivos** un reporte tipo ranking con los **primeros 10 empleados**, ordenados por: **más habilidades avaladas**, **más rotaciones** y **más años en la empresa**. Es un **insumo** para decisiones de ascenso; la **decisión final queda fuera de la app**. |
| **RN-22** | Cada **match / rotación confirmada** informa al empleado que sus rotaciones y mejoras de habilidades **aumentan sus probabilidades** de aparecer en ese reporte. |

---

## 7. Algoritmo de matching (sin IA)

Dos métricas, con **reglas lógicas simples y explicables**. Cualquier usuario puede
entender por qué obtuvo su puntaje.

> ⚠️ **Cambio respecto a versiones anteriores.** El MVP incluía una segunda métrica,
> **Career Impact Score**, que medía qué tanto una vacante acercaba al empleado a su meta de
> carrera (RN-24 original). Esa métrica **se elimina del alcance** (ver PEN-03 resuelto en §9):
> no existirá meta de carrera, escalera de carrera ni el rol VP Engineering. El `RN-24` original
> queda **derogado** y no se reutiliza su numeración. El `Puntaje Final` pasa a depender
> **únicamente** del Match Score.

| ID | Métrica | Definición |
|---|---|---|
| **RN-23** | **Match Score** — ¿tiene las habilidades? | `(% de habilidades avaladas coincidentes × 0.7) + (ajuste por senioridad × 0.3)`. No penaliza fuertemente la falta de 1–2 tecnologías. |
| **RN-25** | **Puntaje Final** | `Puntaje Final = Match Score` (100 %). Al no haber una segunda métrica, no hay pesos que configurar: lo que se evalúa es exclusivamente si el empleado tiene las habilidades. |
| **RN-26** | **Costo de Rotación** | Argumento económico **aproximado** que se le muestra al manager para indicarle los beneficios de una rotación interna: costo estimado si la persona renuncia (recruiting + ramp-up + pérdida de conocimiento) **vs.** costo de moverla internamente (capacitación + transición + baja temporal). Es **puramente informativo**: no entra al cálculo del Puntaje Final ni altera el ranking (PEN-02 resuelto, ver §9). |

### Ejemplo trazable

```
Juan (Senior): Java, SQL, Docker (todas avaladas)
Vacante (Mid-Senior): Go, Kubernetes, Docker
  Habilidades coincidentes: 1 de 3           -> 33%
  Ajuste por senioridad (supera el nivel)    -> 0.9
  Match Score   = (0.33 x 0.7) + (0.9 x 0.3) ~= 50%

  Puntaje Final = Match Score = 50%   -> por debajo del umbral (65%)
```

---

## 8. Umbrales y alertas

| ID | Condición | Acción |
|---|---|---|
| **RN-27** | Puntaje Final **≥ 65 %** | Candidato **"viable"**: se muestra al manager en el ranking. |
| **RN-28** | Puntaje Final **< 65 %** | El sistema **informa al empleado** que sus posibilidades son bajas, pero **NO bloquea** la postulación. El candidato **no aparece** en el ranking del manager. |
| **RN-29** | **2 o más rechazos** del mismo manager a candidatos viables (≥ 65 %) | Se dispara una **alerta a RR.HH.** |
| **RN-30** | Vacante sin Match pasados **10 días** (RN-05) | Notificación al manager solicitante y al Jefe del área. |

---

## 9. Confirmaciones con la empresa

Las tres definiciones que estaban pendientes ya fueron **confirmadas** el 2026-09-21. No hay
pendientes abiertos actualmente.

| ID | Pregunta original | Decisión final | Impacto |
|---|---|---|---|
| **PEN-01** | Umbral de días sin Match para disparar la alerta de vacante abierta. | **10 días**, igual para todos los roles. | RN-05, RN-30, CU_19 |
| **PEN-02** | Si el **Costo de Rotación** (RN-26) se confirma como parte real del algoritmo o se descarta del alcance del MVP. | **Es puramente informativo.** Un valor aproximado que le muestra al manager los beneficios de una rotación interna. **No altera el Puntaje Final ni el ranking.** | RN-26, CU_13 |
| **PEN-03** | Ponderación de los requisitos del Career Impact Score y quién define la escalera de carrera. | **Se elimina del alcance del MVP.** No habrá Career Impact Score, meta de carrera, escalera de carrera ni rol VP Engineering. El Puntaje Final se basa únicamente en el Match Score. | RN-13, RN-24 (derogada), RN-25, CU_04 (eliminado) |

---

## 10. Principio transversal: sin IA

| Beneficio | Por qué |
|---|---|
| **Transparencia** | El empleado y el manager entienden cada número del cálculo. |
| **Confianza** | No hay "caja negra" que acepte o rechace sin explicación. |
| **Mantenibilidad** | Las reglas se ajustan editando pesos y umbrales, no reentrenando modelos. |
| **Rapidez** | Los cálculos son inmediatos. |
| **Equidad** | Ningún dato personal (nombre, edad, género, foto, antigüedad con el manager) entra al cálculo del Puntaje Final. |

---

## 11. Roles del sistema

| Rol | Responsabilidad en las reglas |
|---|---|
| **Empleado (Ingeniero)** | Mantiene su perfil, agrega habilidades (RN-19), se postula (RN-06…RN-11), consulta resultados (RN-18). |
| **Manager / Líder Técnico** | Publica vacantes, avala habilidades de su equipo (RN-19), evalúa peticiones cada 3 días (RN-12), acepta o rechaza (RN-13, RN-17). |
| **Analista de RR.HH.** | Recibe la notificación de aceptación (RN-16), atiende las alertas de rechazo repetido (RN-29) y de vacante sin match (RN-30), carga datos maestros y configura umbrales. |
| **Líder Ejecutivo** | Consume el reporte top 10 (RN-21) como insumo de ascensos. |
| **Administrador del Sistema** | Gestiona usuarios, permisos y datos maestros. |
