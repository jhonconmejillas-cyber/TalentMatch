# Plantilla del README del proyecto 2610

# Nombre de la Aplicación

## Descripción
Breve descripción del sistema de software, el problema que busca solucionar y el contexto general del proyecto.

---

## Equipo del Proyecto
| Nombre         | Rol                   | GitHub / Perfil                 |
|--------------  |-----------------------|---------------------------------|
| Carlos Camacho | Scrum Master          | github.com/charly-31            |
| Jhon Mejia     | Product Owner         | github.com/jhoncomejillas-cyber |
| Juan Maldonado | Sprint Planner        | github.com/camilo058            |
| Jhon Mejia     | Configuration Manager | github.com/jhoncomejillas-cyber |
| Juan Maldonado | QA Lead               | github.com/camilo058            |
| Carlos Camacho | DevOps Engineer       | github.com/charly-31            |

---

## Tecnologías Utilizadas
- **Frontend:** JavaFX
- **Backend:** Java – Spring Boot
- **Base de Datos:** PostgreSQL
- **IA / Data Science:** Python, Pandas, Scikit-learn
- **DevOps:** GitHub Actions, Docker, SonarQube
- **Control de versiones:** Git

---

## Estructura del Proyecto
```text
project-name/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   ├── feature_request.md
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/
│       ├── ci.yml
│       └── cd.yml
├── conf/
│   ├── config.yaml
│   └── settings.json
├── docs/
│   ├── api/
│   ├── architecture/
│   └── user_guide/
├── jupyter/
│   ├── notebooks/
│   │   ├── exploration.ipynb
│   │   └── analysis.ipynb
│   └── datasets/
│       ├── data1.csv
│       └── data2.csv
├── scripts/
│   ├── setup.sh
│   ├── deploy.sh
│   └── test.sh
├── src/
│   ├── main/
│   │   ├── java/ (o python/, etc. según el lenguaje)
│   │   └── resources/
│   ├── test/
│   │   ├── java/ (o python/, etc. según el lenguaje)
│   │   └── resources/
├── temp/
│   ├── temp_file.txt
│   └── temp_data/
│       ├── temp1.tmp
│       └── temp2.tmp
├── .gitignore
├── README.md
├── LICENSE
├── CHANGELOG.md
├── CONTRIBUTING.md
├── Dockerfile
├── docker-compose.yml
└── Makefile
```

---

## Instalación y Ejecución
**Requisitos**
- Docker y Docker Compose
- Git
- Java 17+
- Python 3.10+

## Clonar el repositorio
```text
git clone https://github.com/organizacion/proyecto.git
cd proyecto
```

## Ejecución con Docker
```text
docker-compose up --build
```

## Ejecución de pruebas
```text
docker-compose run backend mvn test
docker-compose run ai-model pytest
```

---

## Contexto Académico
- **Asignatura:** Fundamentos de Ingeniería de Software
- **Docente:** Luis Gabriel Moreno Sandoval, PhD
- **Contacto:** morenoluis@javeriana.edu.co

---

## Contacto

**Equipo de desarrollo:**

**Estudiante 1**  
Estudiante de Ciencia de Datos, Pontificia Universidad Javeriana  
📧 est1.u@javeriana.edu.co  

**Estudiante 2**  
Estudiante de Ingeniería en Sistemas, Pontificia Universidad Javeriana  
📧 est2@javeriana.edu.co  

--- 

## Licencia
Proyecto desarrollado con fines académicos.
