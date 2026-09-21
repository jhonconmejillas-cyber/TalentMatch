# Frontend — TalentMatch

Aplicacion web. Tres paneles segun rol: Empleado, Manager y RR.HH. / Administracion.

## Estructura

| Carpeta | Contenido |
|---|---|
| `src/pages/` | Una pagina por caso de uso principal (perfil, explorar vacantes, ranking, historial…). |
| `src/components/` | Componentes reutilizables (tarjeta de vacante, fila de ranking, desglose de puntaje). |
| `src/services/` | Cliente HTTP hacia el backend. |
| `src/hooks/` | Estado compartido y sesion. |
| `src/styles/` | Estilos globales y tema. |
| `tests/` | Pruebas de componentes. |

## Reglas de interfaz

1. **Maximo 3 clics** hasta cualquier accion principal (RNF-03).
2. **Indicador de carga** para respuestas de mas de 300 ms (HU-005, escenario 3).
3. **Opciones avanzadas ocultas** tras un control explicito (RNF-05).
4. **El ranking no se puede arrastrar ni reordenar** (RN-14, CU_14).
5. Todo mensaje de bloqueo debe decir **cuando** se habilitara la accion (HU-008).
6. Interfaz y mensajes de error **en espanol** (RNF-13).
