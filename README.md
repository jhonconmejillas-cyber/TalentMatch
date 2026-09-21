# TalentMatch

**Plataforma de movilidad interna para ingenieros** — MVP, 4 meses (16 semanas).

TalentMatch conecta ingenieros con proyectos y vacantes dentro de su **propia empresa**, usando
una dinámica inspirada en Tinder pero basada en **reglas lógicas simples y explicables, sin IA**.

| | |
|---|---|
| **Curso** | Fundamentos de Ingeniería de Software (FIS 2610) — Pontificia Universidad Javeriana |
| **Versión** | MVP |
| **Estado** | En desarrollo — fase de especificación |
| **Wiki** | https://github.com/jhonconmejillas-cyber/TalentMatch/wiki |

---

## El problema

Cuando un ingeniero se aburre o siente que dejó de aprender, **no pide un cambio de proyecto**:
busca otra empresa. La organización pierde talento conocido y paga entre 4.000 y 9.000 USD para
reemplazarlo.

## La solución

| Pilar | Qué hace |
|---|---|
| **Visibilidad** | El empleado ve todas las vacantes internas relevantes para su carrera, incluso en otras áreas. |
| **Transparencia** | Cada puntaje es explicable: `Puntaje Final = Match Score × 0.4 + Career Impact × 0.6`. Sin caja negra. |
| **Argumento económico** | El manager ve cuánto cuesta perder a esa persona frente a cuánto cuesta moverla. |
| **Decisión por mérito** | El ranking es inmutable y ningún dato personal entra al cálculo. |

---

## 📚 Documentación del proyecto

Toda la especificación vive en **[`docs/`](docs/)**:

| Documento | Contenido |
|---|---|
| **[Reglas de Negocio](docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md)** | RN-01 … RN-30. **Fuente única de verdad.** |
| **[Requerimientos](docs/requerimientos/RF-RNF-TalentMatch.md)** | 33 RF y 13 RNF, trazados a las reglas de negocio. |
| **[Historias de Usuario](docs/historias-usuario/HU-TalentMatch.md)** | 10 historias con criterios de aceptación, DoD y subtareas. |
| **[Casos de Uso](docs/casos-uso/CU-TalentMatch.md)** | CU_01 … CU_25 en formato estándar, con matriz de trazabilidad. |
| **[Copia del wiki](docs/wiki/)** | Instantánea de las páginas del wiki, versionada junto al código. |

```
Reglas de Negocio (RN)  ->  Requerimientos (RF/RNF)  ->  Casos de Uso (CU)
        ^                                                      |
        +------------------ Historias de Usuario (HU) ---------+
```

Ante cualquier contradicción entre documentos, **prevalecen las Reglas de Negocio**.

---

## 🗂️ Estructura del repositorio

```
TalentMatch/
├── docs/                    Especificación completa del proyecto
│   ├── reglas-negocio/      RN-01 ... RN-30
│   ├── requerimientos/      RF-01 ... RF-33 · RNF-01 ... RNF-13
│   ├── historias-usuario/   HU-001 ... HU-010
│   ├── casos-uso/           CU_01 ... CU_25
│   ├── diagramas/           Diagramas de casos de uso, clases y despliegue
│   └── wiki/                Copia versionada de las páginas del wiki
├── backend/                 API y lógica del algoritmo de matching
│   ├── src/
│   │   ├── api/routes/      Endpoints HTTP
│   │   ├── core/            Configuración, seguridad, sesiones
│   │   ├── models/          Entidades de dominio
│   │   ├── schemas/         Contratos de entrada y salida
│   │   ├── services/        Reglas de negocio (matching, rotación, alertas)
│   │   └── repositories/    Acceso a datos
│   └── tests/               Pruebas unitarias e integración
├── frontend/                Aplicación web
│   ├── public/
│   ├── src/
│   │   ├── assets/ components/ pages/ services/ hooks/ styles/
│   └── tests/
├── database/                Esquema, migraciones y datos de prueba
│   ├── migrations/
│   └── seeds/
├── conf/                    Pesos, umbrales y plazos configurables (RNF-12)
├── scripts/                 Instalación, pruebas y despliegue
└── .github/                 Plantillas de issues y pull requests
```

---

## 🚀 Instalación

```bash
git clone https://github.com/jhonconmejillas-cyber/TalentMatch.git
cd TalentMatch
cp conf/config.example.yaml conf/config.yaml
./scripts/setup.sh
```

```bash
./scripts/test.sh
```

> Los parámetros del algoritmo (pesos 0.4/0.6, umbral 65 %, ciclo de 3 días, plazos de 6 meses
> y 24 horas) se editan en `conf/config.yaml` **sin recompilar** (RNF-12).

---

## 👥 Equipo

| Integrante | Rol(es) | GitHub |
|---|---|---|
| Carlos Camacho | Scrum Master / DevOps Engineer | [@charly-31](https://github.com/charly-31) |
| Jhon Mejia | Product Owner / Configuration Manager | [@jhonconmejillas-cyber](https://github.com/jhonconmejillas-cyber) |
| Juan Maldonado | Sprint Planner / QA Lead | [@camilo058](https://github.com/camilo058) |

---

## 📄 Licencia

[MIT](LICENSE)
