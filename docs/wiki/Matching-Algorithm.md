# Algoritmo de Matching (Sin IA)

El match funciona con **reglas lógicas simples**. Cualquiera puede entender por qué obtuvo su puntaje.

## 4 Métricas Principales

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

### 2. Career Impact Score (¿Te acerca a tu meta?)


**Ejemplo:**
- Meta de Juan: Staff Engineer
  - Requiere: 3+ lenguajes, distributed systems, arquitectura
- Proyecto ofrece: Go (lenguaje nuevo) + Kubernetes (distributed systems)
  - Cubre 2 de 3 requisitos
- **Career Impact = 67%**

---

### 3. Puntaje Final (Ranking que ve el Manager)
**¿Por qué Career Impact tiene mayor peso (0.6)?**

Porque el objetivo es **retención a largo plazo**, no solo llenar vacante hoy.

**Ejemplo:**
- Match Score: 50%
- Career Impact: 67%
- **Puntaje Final = (50 × 0.4) + (67 × 0.6) = 60.2%**

---

### 4. Costo de Rotación (Argumento Económico)
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