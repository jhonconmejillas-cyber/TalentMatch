# Flujo de desarrollo

Cómo organiza el equipo el trabajo: sprints, ramas, commits, Pull Requests, revisiones y releases.

## Sprints

- Sprints de **2 semanas**. Cada sprint es un **Milestone** de GitHub con fecha de cierre.
- Cada issue indica su sprint de dos formas: el milestone y el prefijo `[S1-01]`, `[S2-03]`... en el título.
- El **Sprint 1** construyó la base técnica integrada. A partir de ahí, **el MVP se construye con las historias de usuario**.

| Sprint | Fechas | Objetivo | Estado |
|---|---|---|---|
| Sprint 1 | 28-sep → 11-oct | Base integrada: Spring Boot, frontend, Supabase, login y CI | ✅ 11 issues |
| Sprint 2 | 12-oct → 25-oct | Habilidades y aval (HU-007), vacantes y despliegue | ✅ 3 issues |
| Sprint 3 | 26-oct → 08-nov | Postulación (HU-008), puntaje con bitácora y vacante automática (HU-009) | ✅ 3 issues |
| Sprint 4 | 09-nov → 22-nov | Ranking y decisión (HU-001, HU-004) y resultado anónimo (HU-003) | ✅ 3 issues |
| Sprint 5 | 23-nov → 06-dic | Alertas (HU-002), top 10 (HU-010), rendimiento y privacidad (HU-005, HU-006) | ✅ 4 issues → **v1.0.0** |

## Tablero kanban (GitHub Projects)

`Backlog` → `Ready` → `In progress` → `In review` → `Done`. Cada issue lleva un **Estimate** en puntos; sin estimación, las gráficas de *Insights* salen planas.

## Ramas (Gitflow)

```
main      o--------------o--------------o        versión entregada (tag vX.Y.Z), producción
           \            /              /
develop     o--o--o--o-o--o--o--o--o--o          integración del equipo
               \   /      \      /
feature/...     o-o        o----o                una rama por issue
```

| Rama | Sale de | Se fusiona en | Uso |
|---|---|---|---|
| `feature/<issue>-<descripcion>` | `develop` | `develop` (PR) | Trabajo de un issue. Ej.: `feature/26-puntaje-final` |
| `fix/<descripcion>` / `chore/<descripcion>` | `develop` | `develop` (PR) | Correcciones y tareas sin issue propio |
| `release/<versión>` | `develop` | `main` (PR) y de vuelta a `develop` | Preparar una versión (CHANGELOG). Ej.: `release/1.0.0` |
| `hotfix/<descripcion>` | `main` | `main` y `develop` | Error urgente en producción |
| `develop` | — | `main` (PR de release) | — |

**Reglas:**
1. Nadie hace commit directo en `main` ni en `develop`: todo entra por Pull Request. Las ramas están protegidas y exigen el check del CI.
2. Un PR de `feature/*` **siempre apunta a `develop`**.
3. Un issue = una rama = un PR. La descripción incluye `Closes #<número>`.
4. **Fusionar a `main` = desplegar a producción.** Render y Vercel despliegan solos.

## Commits convencionales

Formato: `tipo(alcance): descripción en minúscula e imperativo`, en español.

| Tipo | Uso | Ejemplo real |
|---|---|---|
| `feat` | Funcionalidad nueva | `feat(ranking): HU-001 revisar el ranking de candidatos internos` |
| `fix` | Corrección | `fix(ranking): contar los dias del ciclo con fechas con zona horaria` |
| `test` | Pruebas | `test: plan de pruebas unitarias de CalculadoraMatchScore, Vacante y PostulacionService` |
| `ci` | Integración continua | `ci: informe de pruebas como artefacto, cobertura con JaCoCo y analisis en SonarQube Cloud` |
| `chore` | Configuración, despliegue, dependencias | `chore(deploy): desplegar el frontend en Vercel y el backend en Render` |
| `docs` | Documentación | `docs(pruebas): plan de pruebas e informe del taller` |
| `perf` | Rendimiento | `perf: HU-005 tiempo de respuesta del ranking` |
| `refactor` / `style` | Cambios sin efecto funcional | — |

## Ciclo de un Pull Request

1. Crea la rama desde `develop` actualizado: `git checkout develop && git pull && git checkout -b feature/30-mi-cambio`.
2. Programa con sus pruebas. Ejecuta `./mvnw verify` en local si tocaste el backend (ver [Pruebas](Testing-and-Quality)).
3. `git push -u origin feature/30-mi-cambio` y abre el PR hacia `develop`, con `Closes #30` y un resumen.
4. Se ejecutan automáticamente (ver [GitHub Actions y CI/CD](CI-CD-and-GitHub-Actions)):
   - **CI**: compilar, 203 pruebas, informe, cobertura y SonarQube. **Obligatorio**.
   - **Construir imagen Docker**.
   - **SonarCloud Code Analysis**: el *quality gate* del código nuevo.
   - **CodeRabbit**: revisión automática con comentarios en español.
   - **Vercel**: *preview* del frontend con su propia URL.
5. Atiende los comentarios de CodeRabbit y del equipo. Si no aplicas una sugerencia, **responde en el hilo con el motivo**.
6. Al tener todo en verde y la revisión aprobada, se fusiona con **Merge commit**.

## Releases

1. Rama `release/X.Y.Z` desde `develop`. Pasa la sección `[Sin publicar]` de `CHANGELOG.md` a `[X.Y.Z] - fecha`.
2. PR `release/X.Y.Z` → `main`. Al fusionar, producción se actualiza sola.
3. Tag y release en GitHub: `git tag -a vX.Y.Z origin/main -m "..." && git push origin vX.Y.Z` y luego `gh release create vX.Y.Z --notes-file notas.md`.
4. PR `release/X.Y.Z` → `develop`, para devolver el CHANGELOG.
5. Cierra el milestone.

Versión actual: **v1.0.0** (MVP de los Sprints 1 a 5).

## Validación del Product Owner

Cada historia debe validarla el Product Owner contra sus criterios de aceptación (`docs/historias-usuario/`). Los PRs del MVP se fusionaron como administrador para avanzar, así que la validación funcional del PO **queda pendiente**: conviene hacerla sobre producción con los usuarios de prueba.
