# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [Sin publicar]

### Agregado
- Estructura de carpetas del proyecto: `docs/`, `backend/`, `frontend/`, `database/`, `conf/`, `scripts/`.
- `docs/reglas-negocio/` — 30 reglas de negocio (RN-01 … RN-30).
- `docs/requerimientos/` — 33 requerimientos funcionales numerados (32 activos) y 13 no funcionales.
- `docs/historias-usuario/` — 10 historias de usuario.
- `docs/casos-uso/` — 25 casos de uso numerados (24 activos, CU_01 … CU_25) con matriz de trazabilidad completa.
- `docs/wiki/` — copia versionada de las paginas del wiki.
- `conf/config.example.yaml` — pesos, umbrales y plazos configurables sin recompilar (RNF-12).
- `docs/diagramas/DC-TalentMatch-Diagrama-de-Clases.md` — modelo de clases en dos notaciones:
  Entidad-Relación (Pata de Cuervo) y UML, como diagramas Mermaid versionados con el código.

### Cambiado
- **Reglas de negocio:** el manager ya no da feedback al rechazar. El empleado rechazado ve el
  porcentaje y las habilidades del seleccionado, sin su nombre (RN-17, RN-18).
- **Reglas de negocio:** el bloqueo de repostulacion pasa de 3 a **6 meses**, y ademas exige que
  se abra una nueva rotacion en ese puesto (RN-10).
- **Requerimientos no funcionales:** metas simplificadas para el alcance del MVP
  (500 -> 50 usuarios concurrentes; uptime 99% -> 95% en horario laboral; cifrado en reposo ->
  hash de contrasenas + HTTPS; SLA humano de 24 h -> notificacion automatica en < 5 min).
- **Historias de usuario:** HU-003 reescrita por completo; HU-002, HU-004, HU-005 y HU-006
  corregidas contra las reglas vigentes.
- **Reglas de negocio (confirmaciones con la empresa, 2026-09-21):** PEN-01 se confirma en
  **10 días** de umbral sin match (RN-05, RN-30). PEN-02 se confirma: el Costo de Rotación
  (RN-26) es **puramente informativo**, un valor aproximado para mostrarle al manager los
  beneficios de la rotación interna; no altera el Puntaje Final ni el ranking. PEN-03 se
  confirma **eliminando** el Career Impact Score del alcance del MVP: el Puntaje Final pasa a
  depender únicamente del Match Score (RN-13, RN-25).

### Eliminado
- Archivos de plantilla del boilerplate (`BOILERPLATE_template.md`, `jdf`, configuracion vacia).
- RF de metricas de desempeno individual (velocidad historica, historial de bloqueos): no entran
  al algoritmo y contradicen la neutralidad del calculo (RNF-09).
- Flujo de apelacion: las reglas de negocio no lo contemplan. Lo sustituye la alerta automatica
  a RR.HH. por rechazos repetidos (RN-29).
- **Career Impact Score** del alcance del MVP (PEN-03 confirmado), junto con la meta de carrera,
  la escalera de carrera, el rol **VP Engineering**, `RF-05` (Definir meta de carrera) y
  `CU_04` (Definir meta de carrera). Los identificadores `RN-24`, `RF-05` y `CU_04` quedan
  reservados y no se reutilizan.
