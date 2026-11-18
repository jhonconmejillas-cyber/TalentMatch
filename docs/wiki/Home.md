# TalentMatch: wiki del proyecto

TalentMatch es una **plataforma web de movilidad interna de talento**. Los empleados de una empresa se postulan a vacantes internas, el sistema calcula un **Puntaje Final** objetivo a partir de sus **habilidades avaladas** y su **senioridad**, y el manager decide sobre un **ranking que nadie puede reordenar**. Todo se resuelve con reglas simples y auditables, sin inteligencia artificial.

Esta wiki está escrita para que **cualquier persona que llegue al proyecto y tenga que hacer cambios** encuentre el contexto completo: cómo está construido, por qué se tomó cada decisión, cómo se prueba, cómo se despliega y qué pasó en cada etapa del proyecto.

---

## Estado actual

| Dato | Valor |
|---|---|
| **Versión** | `v1.0.0`, MVP completo de los Sprints 1 a 5 ([release](https://github.com/jhonconmejillas-cyber/TalentMatch/releases/tag/v1.0.0)) |
| **Backlog** | 24 issues de los 5 sprints cerrados (HU-001 a HU-010 y las tareas técnicas S1 a S5) |
| **Pruebas** | 203 pruebas JUnit 5 en verde, 89.9 % de cobertura del código de producción del backend |
| **Calidad** | Quality gate de SonarQube Cloud aprobado, 0 bugs y 0 code smells |
| **Frontend en producción** | https://talentmatch-frontend-puce.vercel.app (Vercel) |
| **API en producción** | https://talentmatch-n5kp.onrender.com/api/v1 (Render). Health: `/api/v1/health` |
| **Base de datos** | PostgreSQL en Supabase |
| **Usuarios de prueba** | 16 usuarios, todos con la contraseña `talento` (ver [Despliegue](Deployment#usuarios-de-prueba)) |

---

## Por dónde empezar

**Si vas a programar:**
1. [Instalación y entorno local](Installation-and-Setup): levantar el proyecto en tu máquina.
2. [Arquitectura técnica](Technical-Architecture): las 4 capas y cómo viaja una petición.
3. [Guía del backend](Backend): cada paquete y cómo agregar una funcionalidad.
4. [Flujo de desarrollo](Development-Workflow): ramas, PR, revisiones y CI.

**Si vas a entender el negocio:**
1. [Visión general](Project-Overview) y [Problema y solución](Problem-and-Solution).
2. [Roles y flujos](User-Roles-and-Flows).
3. [Algoritmo de matching](Matching-Algorithm).
4. [Mapa de reglas de negocio](Business-Rules-Map): en qué clase y en qué prueba vive cada regla RN-xx.

**Si vas a desplegar u operar:**
1. [Despliegue: Vercel + Render + Supabase](Deployment).
2. [GitHub Actions y CI/CD](CI-CD-and-GitHub-Actions).
3. [Configuración y variables de entorno](Configuration).
4. [Solución de problemas](Troubleshooting).

---

## Índice completo

| Área | Página | Contenido |
|---|---|---|
| Producto | [Visión general](Project-Overview) | Qué es, alcance del MVP, equipo y estado |
| Producto | [Problema y solución](Problem-and-Solution) | Por qué existe TalentMatch |
| Producto | [Stakeholders](Stakeholders) | Quién está alrededor del sistema |
| Producto | [Roles y flujos](User-Roles-and-Flows) | Qué hace cada uno de los 5 roles, pantalla por pantalla |
| Producto | [Algoritmo de matching](Matching-Algorithm) | Fórmula del Match Score, umbral y ranking |
| Producto | [Mapa de reglas de negocio](Business-Rules-Map) | RN-01 a RN-30: dónde están implementadas y probadas |
| Técnico | [Arquitectura técnica](Technical-Architecture) | Stack, capas, ciclo de una petición y decisiones de diseño |
| Técnico | [Guía del backend](Backend) | Paquetes, clases, convenciones y cómo extender |
| Técnico | [Referencia de la API](API-Reference) | Todos los endpoints con su rol y su respuesta |
| Técnico | [Modelo de datos](Database-Model) | Tablas, restricciones, migraciones Flyway y datos de prueba |
| Técnico | [Guía del frontend](Frontend) | Páginas, módulos JS, sesión y cómo agregar una pantalla |
| Técnico | [Seguridad](Security) | Login, token, roles, permisos por relación y CORS |
| Técnico | [Configuración](Configuration) | `conf/config.yaml`, `application.yml` y variables de entorno |
| Calidad | [Pruebas y calidad](Testing-and-Quality) | Suite JUnit, plan de pruebas, cobertura, SonarQube y CodeRabbit |
| Operación | [GitHub Actions y CI/CD](CI-CD-and-GitHub-Actions) | Workflows, checks, protección de ramas y artefactos |
| Operación | [Despliegue](Deployment) | Vercel, Render y Supabase paso a paso |
| Operación | [Instalación local](Installation-and-Setup) | Entorno de desarrollo |
| Equipo | [Flujo de desarrollo](Development-Workflow) | Gitflow, commits, PR, revisiones y releases |
| Equipo | [Historial del proyecto](Project-History) | Todo lo que se hizo, en orden, con los PRs |
| Equipo | [Decisiones e interpretaciones](Design-Decisions) | Lo que la especificación no definía y cómo se resolvió |
| Equipo | [Solución de problemas](Troubleshooting) | Errores conocidos y cómo resolverlos |

---

## Documentación formal (en el repositorio)

La wiki **explica**; la especificación **manda**. Si hay una contradicción, prevalecen estos documentos de la carpeta `docs/` del repositorio:

| Documento | Ruta |
|---|---|
| Reglas de negocio (RN-01 a RN-30) | `docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md` |
| Requerimientos funcionales y no funcionales | `docs/requerimientos/RF-RNF-TalentMatch.md` |
| Historias de usuario (HU-001 a HU-010) | `docs/historias-usuario/HU-TalentMatch.md` |
| Casos de uso (CU_01 a CU_25) | `docs/casos-uso/CU-TalentMatch.md` |
| Diagrama de clases | `docs/diagramas/DC-TalentMatch-Diagrama-de-Clases.md` |
| Matriz de permisos por rol | `docs/seguridad/Matriz-de-Permisos.md` |
| Plan de pruebas e informe del taller | `docs/pruebas/` |

Una copia de esta wiki se guarda también en `docs/wiki/` para que la documentación viaje con el código.
