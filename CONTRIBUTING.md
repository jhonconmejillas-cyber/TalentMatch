# Guia de contribucion — TalentMatch

## Flujo de trabajo (Gitflow)

Guia completa en el wiki: [Development Workflow](https://github.com/jhonconmejillas-cyber/TalentMatch/wiki/Development-Workflow).

```
main      o--------------o--------------o        version entregada al cierre de cada sprint (tag)
           \            /              /
develop     o--o--o--o-o--o--o--o--o--o          integracion del equipo
               \   /      \      /
feature/...     o-o        o----o                una rama por issue
```

| Rama | Sale de | Se fusiona en | Cuando |
|---|---|---|---|
| `feature/<issue>-<descripcion>` | `develop` | `develop` (PR) | Al trabajar un issue. Ej.: `feature/27-avalar-habilidades` |
| `develop` | — | `main` (PR) | Al cerrar el sprint. Se crea un tag `vX.Y.Z` |
| `hotfix/<descripcion>` | `main` | `main` y `develop` | Error urgente en la version entregada |

1. `main` y `develop` estan protegidas: **nadie hace commit directo**, todo entra por PR con
   al menos **1 aprobacion**.
2. Un PR de `feature/*` **siempre apunta a `develop`**, nunca a `main`.
3. Un issue = una rama = un PR. No abrir PRs duplicados de la misma rama.
4. La descripcion del PR incluye `Closes #<numero>` para cerrar el issue y moverlo a *Done*.
5. Todo PR debe pasar el CI (compilacion y pruebas JUnit) antes de fusionarse.

### Como empezar un issue

```bash
git switch develop
git pull
git switch -c feature/27-avalar-habilidades
# ... trabajar y hacer commits ...
git push -u origin feature/27-avalar-habilidades
```

Luego abrir el PR hacia `develop` y mover la tarjeta a **In review**.

## Sprints y tablero

- Cada sprint (2 semanas) es un **Milestone**; cada issue lleva su milestone y el prefijo
  `[S1]`, `[S2]`… en el titulo.
- Tablero kanban: `Backlog` → `Ready` → `In progress` → `In review` → `Done`.
- Cada issue debe tener un **Estimate**; sin el, las graficas de *Insights* salen planas.

## Mensajes de commit

Se usa [Conventional Commits](https://www.conventionalcommits.org/es/):
`tipo(alcance): descripcion en minuscula e imperativo`.

| Tipo | Uso |
|---|---|
| `feat` | Funcionalidad nueva |
| `fix` | Correccion de error |
| `docs` | Documentacion |
| `test` | Pruebas |
| `refactor` | Cambio interno sin cambiar comportamiento |
| `chore` | Configuracion, dependencias |
| `ci` | Integracion continua y despliegue |
| `style` | Formato, sin cambio de logica |

```
feat(habilidades): permitir al manager avalar una habilidad
fix(vacante): respetar la activacion diferida de 24h
docs(reglas-negocio): actualizar RN-10 tras confirmacion con la empresa
```

## Antes de abrir un PR que toque la logica de negocio

Verifica que tu cambio sea consistente con la documentacion, **en este orden**:

1. [Reglas de Negocio](docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) — manda sobre todo lo demas.
2. [Requerimientos](docs/requerimientos/RF-RNF-TalentMatch.md)
3. [Casos de Uso](docs/casos-uso/CU-TalentMatch.md)

Si tu cambio **contradice** una regla de negocio, el PR no se fusiona: primero se actualiza la
regla, y con ella los requerimientos, las historias y los casos de uso afectados.

## Reglas que ninguna contribucion puede romper

| Regla | Donde se verifica |
|---|---|
| Solo las habilidades `avalado` entran al calculo (RN-20). | Prueba unitaria del Match Score. |
| Ningun dato personal interviene en el calculo (RNF-09). | Lista cerrada de campos en `backend/src/services/`. |
| Todo calculo queda en bitacora antes de devolverse (RNF-06). | CU_11, excepcion del paso 6. |
| El ranking nunca se reordena manualmente (RN-14). | Prueba de la API y de la UI. |
| La respuesta de rechazo no expone la identidad del seleccionado (RN-18). | Prueba automatica sobre el JSON de respuesta. |
