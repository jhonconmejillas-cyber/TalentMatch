# Roles de Usuario & Flujos

## 3 Roles Principales

### 1. 👨‍💻 Empleado (Ingeniero)

**Qué hace:**
- Define su perfil: habilidades, senioridad, años de experiencia
- Define meta de carrera (ej. Staff Engineer)
- Guarda "deseos" de habilidades que quiere aprender
- Explora vacantes activas
- Expresa interés en vacantes relevantes
- Si es rechazado, ve el % y las habilidades de quien fue seleccionado (sin su nombre) — no recibe feedback directo del manager
- Participa en pulsos trimestrales de satisfacción

**Dashboard principal:**
- Progreso hacia su meta de carrera
- Vacantes recomendadas
- Deseos guardados
- Historial de aplicaciones

---

### 2. 👔 Manager / Líder Técnico

**Qué hace:**
- Publica vacantes del equipo
- Revisa ranking de candidatos (ordenado por Puntaje Final)
- Ve Match Score + Career Impact + Costo de Rotación
- Acepta o rechaza candidatos
- Si rechaza candidato de match ≥65%, el sistema notifica automáticamente al empleado (% y habilidades del seleccionado, sin nombre) — el manager no da feedback manual
- Cada 12 meses justifica por qué empleados continúan en el proyecto

**Panel principal:**
- Vacantes activas
- Candidatos por vacante (rankeados)
- Análisis de costo de rotación
- Histórico de decisiones

---

### 3. 🏢 RR.HH. & VP Engineering

**Qué hacen RR.HH.:**
- Dan seguimiento a alertas de rechazo repetido
- Cargan datos de la empresa (CSV)
- Configuran umbrales y pesos del algoritmo
- Colaboran en definición de escaleras de carrera

**Qué hace VP Engineering:**
- Define requisitos técnicos de cada nivel de carrera
- Colabora con RR.HH. en la escalera

**Panel administrativo:**
- Carga de datos (empleados, proyectos, skills)
- Definición de escaleras de carrera
- Configuración de pesos y umbrales
- Alertas de problemas (rechazo repetido, baja actividad)

---

## Flujos Detallados

### Flujo del Empleado

1. **Setup inicial**
   - Completa perfil (skills, senioridad, años)
   - Elige meta de carrera desde escalera predefinida
   
2. **Exploración**
   - Ve vacantes activas
   - Cada vacante muestra: Match Score, Career Impact, tiempo de aprendizaje
   
3. **Interés**
   - Guarda "deseos" de habilidades (wishlist)
   - Expresa interés en vacante (como "swipe" en Tinder)
   
4. **Resultado**
   - Si acepta manager: se inicia onboarding al nuevo proyecto
   - Cada match/rotación confirmada le informa al empleado que sus rotaciones y mejoras de habilidades aumentan sus probabilidades de aparecer en el reporte de recomendación (top 10 por antigüedad/habilidades/rotaciones) que ven los líderes ejecutivos para decisiones de ascenso
   - Si rechaza: NO recibe feedback directo del manager. El sistema le muestra el % y las habilidades/conocimientos de la persona que sí fue seleccionada, sin mostrar su nombre
   - Espera 6 meses antes de reaplicar a la misma vacante

5. **Engagement**
   - Pulso trimestral (1 tap: Bien/Neutral/Mal)
   - Checkpoint anual: decide si continuar o explorar

---

### Flujo del Manager

1. **Publicar vacante**
   - Define skills críticas, deseables, nivel de senioridad
   
2. **Revisar candidatos**
   - Ve ranking de empleados que expresaron interés
   - Por cada uno: Match Score, Career Impact, Costo de Rotación
   
3. **Decisión**
   - Acepta: se inicia transición formal con RR.HH.
   - Rechaza: el sistema notifica automáticamente al empleado con el % y las habilidades del candidato seleccionado (sin nombre) — el manager no da feedback manual
   
4. **Monitoreo**
   - Si rechaza 2+ veces a alguien viable → alerta a RR.HH.
   - Checkpoint anual: justifica por qué empleados continúan

---

## El "Match" (como Tinder)

**Match = ambas partes dicen sí**

```
Empleado expresa interés ← Sistema + Match Scoring → Manager revisa
                    ↓
            ¿Match ≥ 65%?
                ↙        ↘
            SÍ           NO (no mostrar al manager)
            ↓
        Manager ve ranking
            ↓
      Acepta o Rechaza
            ↓
        Si ACEPTA → Match Confirmado ✅
        Si RECHAZA + match alto → Empleado ve % y habilidades del seleccionado (anónimo)
```