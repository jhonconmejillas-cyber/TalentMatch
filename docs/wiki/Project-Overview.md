# Visión general

## ¿Qué es TalentMatch?

TalentMatch es una plataforma de **movilidad interna de talento** para empresas de ingeniería de software:

- ✅ Conecta a los empleados con vacantes de **otros proyectos o áreas** dentro de su propia empresa.
- ✅ Calcula un **Puntaje Final** objetivo con reglas simples y explicables, **sin IA**.
- ✅ Muestra al manager el **costo económico** de perder talento frente a moverlo internamente.
- ✅ Protege al empleado: ranking por mérito que nadie puede reordenar, resultado anónimo y alertas a RR.HH. ante rechazos sospechosos.

## El diferenciador

| Aspecto | TalentMatch |
|---|---|
| **Sin caja negra** | La fórmula es pública y parametrizable, y cada cálculo queda en una bitácora inmutable |
| **Mérito, no cercanía** | Solo cuentan las habilidades **avaladas** por el manager actual. El ranking no se puede reordenar y RR.HH. recibe una alerta si un manager rechaza repetidamente a candidatos viables |
| **Argumento económico** | El desglose muestra al manager cuánto ahorra rotando internamente frente a contratar afuera |
| **Descubrimiento activo** | La wishlist avisa al empleado cuando aparece una vacante con lo que quiere aprender |
| **Transparencia sin exponer a nadie** | Quien no fue seleccionado ve el puntaje y las habilidades del elegido, pero nunca su identidad |

## Objetivos

1. **Retener talento:** evitar que los ingenieros renuncien por estancamiento.
2. **Reducir costos de rotación:** ahorrar en reclutamiento y *ramp-up*.
3. **Visibilizar oportunidades:** los empleados ven dónde pueden crecer.
4. **Decisiones basadas en datos:** el manager decide con un ranking objetivo y el costo real.

## Alcance del MVP (v1.0.0)

| Incluido | Fuera del alcance |
|---|---|
| Registro y login con 5 roles | Integración con nómina o con el ERP de RR.HH. |
| Perfil, habilidades y aval del manager | La comunicación humana del cambio (la hace RR.HH. fuera de la app) |
| Vacantes manuales y automáticas (al liberarse un puesto) | La decisión de contratar externamente o eliminar el puesto |
| Postulación con reglas (6 meses, repostulación) | El Career Impact Score (eliminado, PEN-03) |
| Puntaje Final con bitácora | Decisiones de ascenso (el top 10 es solo un insumo) |
| Ranking inmutable, aceptar y rechazar, ciclo de 3 días | Aplicación móvil |
| Resultado anónimo, alertas y reportes (rechazos, top 10) | — |
| Seguridad: permisos por rol y por relación, bitácora de accesos y sesión de 30 min | — |

## Estado

- **v1.0.0** publicada; los 24 issues de los 5 sprints están cerrados.
- **En producción:** https://talentmatch-frontend-puce.vercel.app (ver [Despliegue](Deployment)).
- **Pruebas:** 203 en verde y 89.9 % de cobertura; *quality gate* de SonarQube aprobado.
- **Pendiente:** validación del Product Owner (ver [Historial](Project-History#pendientes)).

## Equipo

| Integrante | Rol(es) | GitHub |
|---|---|---|
| Carlos Camacho | Scrum Master / DevOps Engineer | [@charly-31](https://github.com/charly-31) |
| Jhon Mejía | Product Owner / Configuration Manager | [@jhonconmejillas-cyber](https://github.com/jhonconmejillas-cyber) |
| Juan Maldonado | Sprint Planner / QA Lead | [@camilo058](https://github.com/camilo058) |

Curso: Fundamentos de Ingeniería de Software, Pontificia Universidad Javeriana.

## Visión a futuro

- **5 años:** expandirse a otras áreas además del software (ingeniería, salud, logística) donde exista rotación interna por proyectos.
- **Región:** Latinoamérica, con foco en *startups* en crecimiento.
