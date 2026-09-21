# Problema & Solución

## El Problema

### Causa Raíz
Cuando un ingeniero se aburre o siente que dejó de aprender, **no pide un cambio de proyecto**: simplemente busca otra empresa.

### Por Qué Pasa
1. **Invisibilidad** — El empleado no sabe qué oportunidades existen en su empresa
2. **Desincentivo del manager** — Los líderes retienen a su mejor gente por instinto, aunque sea perjudicial para la empresa
3. **Falta de argumento económico** — Nadie le dice al manager el costo real de perder al talento

### Consecuencias
- 💸 Empresa pierde talento conocido + paga $4K–$9K en recontratación
- 🔴 Proyectos quedan incompletos
- 😞 Ingenieros se van frustrados por falta de crecimiento visible

---

## La Solución: TalentMatch

### Tres Pilares

#### 1️⃣ **Visibilidad**
- Empleado ve todas las vacantes internas relevantes para su carrera
- Sistema de "deseos" notifica cuando aparece lo que busca (proactivo, tipo Tinder)

#### 2️⃣ **Transparencia en el Match**
- Match Score muestra si tienes las habilidades
- Career Impact Score muestra si te acerca a tu meta de carrera
- Sin "caja negra" — todo es explicable

#### 3️⃣ **Argumento Económico para el Manager**
- Reporte de Costo de Rotación: "Si pierdo este talento, costo $X. Si lo muevo, costo $Y. Ahorro: $Z"
- Si rechaza a candidato de alto match, el sistema le muestra automáticamente el % y las habilidades de quien sí fue seleccionado (anónimo)
- Alertas a RR.HH. si rechaza 2+ veces a alguien viable

---

## Cómo Funciona (Flujo Simple)

```
Empleado                      Sistema                      Manager
   |                             |                            |
   └──Define meta (Staff Eng)─────>                           |
   |                             |                            |
   └──Define deseos (Kubernetes)─>                           |
   |                             |                            |
   |                         Vacante aparece                  |
   |<─────Notificación proactiva──|                           |
   |                             |                            |
   └──Expresa interés───────────────────────────────────────>|
   |                             |     Manager ve ranking     |
   |                             |     + Costo de Rotación    |
   |                             |                            |
   |<────────Match Aceptado────────────────Decisión del manager─|
   |                             |                            |
   └──Se inicia transición───────> Notifica a RR.HH. para    |
   |                             |  formalizar cambio         |
```