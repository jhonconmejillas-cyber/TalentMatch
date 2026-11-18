# Algoritmo de Matching (Sin IA)

El match funciona con **reglas lógicas simples**. Cualquiera puede entender por qué obtuvo su puntaje.

> ⚠️ El algoritmo incluía originalmente una segunda métrica, **Career Impact Score** (qué tanto
> una vacante acercaba al empleado a su meta de carrera). Esa métrica, la meta de carrera, la
> escalera de carrera y el rol VP Engineering **se eliminaron del alcance del MVP**. El Puntaje
> Final depende ahora **únicamente** del Match Score.

## 2 Métricas Principales

### 1. Match Score (¿Tiene las habilidades?)

Combina:
- % de habilidades técnicas que coinciden
- Ajuste por experiencia/senioridad (no penaliza falta de 1-2 techs)

**Ejemplo:**
- Juan: Java, SQL, Docker (Senior)
- Vacante: Go, Kubernetes, Docker (Mid-Senior)
- Skills coincidentes: 1 de 3 → 33%
- Ajuste senioridad: 0.9 (supera nivel)
- **Match Score = (0.33 × 0.7) + (0.9 × 0.3) ≈ 50%**

---

### 2. Puntaje Final (Ranking que ve el Manager)

`Puntaje Final = Match Score`. No hay una segunda métrica que ponderar: lo único que se evalúa
es si el empleado tiene las habilidades.

**Ejemplo:**
- Match Score: 50%
- **Puntaje Final = 50%**

---

### Costo de Rotación (Argumento Económico, solo informativo)

Es un valor **aproximado**, mostrado únicamente al manager para indicarle los beneficios de una
rotación interna. **No entra al Puntaje Final ni altera el ranking.**

**Ejemplo:**
- Si Juan renuncia: $6,000 (recruiting) + $3,000 (ramp-up) + $2,000 (pérdida) = **$11,000**
- Si Juan se mueve: $1,000 (capacitación) + $500 (transición) + $500 (baja temporal) = **$2,000**
- **Ahorro: $9,000**

**El manager ve:** "Si aceptas a Juan, ahorras ~$9K comparado con recontratarlo"

---

## Umbrales Clave

| Threshold | Acción |
|-----------|--------|
| Puntaje ≥ 65% | "Candidato viable" — se muestra al manager |
| Puntaje < 65% | Sistema informa al empleado "posibilidades bajas" pero NO bloquea solicitud |
| Match ≥ 65% + Manager rechaza | Sistema muestra al empleado el % y habilidades del seleccionado (sin nombre) — no hay feedback manual del manager |
| 2+ rechazos del mismo manager a empleado viable | Alerta a RR.HH. |

---

## ¿Por Qué Sin IA?

✅ **Transparencia** — empleado/manager entiende cada número  
✅ **Confianza** — no hay "caja negra" que rechace o acepte  
✅ **Mantenibilidad** — las reglas se ajustan fácilmente  
✅ **Rapidez** — cálculos son inmediatos, sin entrenamientos de modelos