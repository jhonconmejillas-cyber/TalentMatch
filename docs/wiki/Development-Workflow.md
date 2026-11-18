# Flujo de Trabajo

Cómo organiza el equipo el trabajo: sprints, ramas, commits y tablero.

## Sprints

- Sprints de **2 semanas**. Cada sprint es un **Milestone** de GitHub con fecha de cierre.
- Cada issue indica su sprint de dos formas: el **milestone** y el prefijo `[S1]`, `[S2]`… en el título.
- El **Sprint 1** construye la base técnica integrada (frontend + backend + base de datos + CI).
  Las historias de usuario se implementan a partir de ahí: **el MVP se construye con las HU**,
  no al revés.

| Sprint | Fechas | Objetivo |
|---|---|---|
| Sprint 1 | 28-sep → 11-oct | Base integrada: Spring Boot, frontend, Supabase, login, CI |
| Sprint 2 | 12-oct → 25-oct | Habilidades y aval (HU-007), vacantes |
| Sprint 3 | 26-oct → 08-nov | Postulación (HU-008), puntaje con bitácora, vacante automática (HU-009) |
| Sprint 4 | 09-nov → 22-nov | Ranking y decisión (HU-001, HU-004), resultado anónimo (HU-003) |
| Sprint 5 | 23-nov → 06-dic | Alertas (HU-002), top 10 (HU-010), rendimiento y privacidad (HU-005, HU-006) → **v1.0.0 MVP** |

## Tablero kanban (GitHub Projects)

`Backlog` → `Ready` → `In progress` → `In review` → `Done`

| Columna | Significa |
|---|---|
| Backlog | Aún no se planea para el sprint actual |
| Ready | Planeada para el sprint, lista para tomarse |
| In progress | Alguien la está trabajando en su rama |
| In review | Tiene un PR abierto esperando revisión |
| Done | El PR se fusionó en `develop` |

Cada issue lleva un **Estimate** (puntos). Sin estimación, las gráficas de *Insights* salen planas.

## Ramas (Gitflow)

```
main      o--------------o--------------o        versión entregada al cierre de cada sprint (tag)
           \            /              /
develop     o--o--o--o-o--o--o--o--o--o          integración del equipo
               \   /      \      /
feature/...     o-o        o----o                una rama por issue
```

| Rama | Sale de | Se fusiona en | Cuándo |
|---|---|---|---|
| `feature/<issue>-<descripcion>` | `develop` | `develop` (PR) | Al trabajar un issue. Ej.: `feature/15-avalar-habilidades` |
| `develop` | — | `main` (PR) | Al cerrar el sprint, con todo probado. Se crea un tag `vX.Y.Z` |
| `hotfix/<descripcion>` | `main` | `main` y `develop` | Error urgente en la versión entregada |

**Reglas:**

1. Nadie hace commit directo en `main` ni en `develop`: todo entra por Pull Request.
2. Un PR de `feature/*` **siempre apunta a `develop`**, nunca a `main`.
3. Un issue = una rama = un PR. No abrir PRs duplicados de la misma rama.
4. La descripción del PR incluye `Closes #<número>` para cerrar el issue automáticamente.

## Commits convencionales

Formato: `tipo(alcance): descripción en minúscula e imperativo`

| Tipo | Uso | Ejemplo |
|---|---|---|
| `feat` | Funcionalidad nueva | `feat(habilidades): permitir al manager avalar una habilidad` |
| `fix` | Corrección de error | `fix(ranking): corregir orden con puntajes empatados` |
| `docs` | Documentación | `docs(rn): actualizar RN-19` |
| `test` | Pruebas | `test(matching): cubrir cálculo con habilidades no avaladas` |
| `refactor` | Cambio interno sin cambiar comportamiento | `refactor(postulacion): extraer validación de 6 meses` |
| `chore` | Configuración, dependencias | `chore(backend): agregar dependencia de Flyway` |
| `ci` | Integración continua | `ci: ejecutar pruebas JUnit en cada PR` |
| `style` | Formato, sin cambio de lógica | `style(frontend): ordenar estilos del login` |

## Cierre de sprint

1. Todos los issues del milestone están en **Done** (o se mueven al siguiente sprint).
2. PR `develop` → `main`, revisado por el equipo.
3. Tag de versión en `main` (`v0.1.0`, `v0.2.0`… y `v1.0.0` para el MVP).
4. Actualizar `CHANGELOG.md`.
5. Cerrar el milestone.
