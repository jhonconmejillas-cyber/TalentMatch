# Algoritmo de matching

TalentMatch **no usa inteligencia artificial**. El puntaje es una fórmula simple, pública y parametrizable, para que cualquiera pueda recalcularlo a mano (auditabilidad, RNF-06) y para que nada personal influya (neutralidad, RNF-09).

## Fórmula (RN-23, RN-25)

```
Match Score  = ( % habilidades requeridas que el empleado tiene AVALADAS × peso_habilidades
               + ajuste por senioridad                                × peso_senioridad ) × 100

Puntaje Final = Match Score          (redondeado a 1 decimal)
```

| Parámetro (`conf/config.yaml`) | Valor |
|---|---|
| `peso_habilidades` | 0.7 |
| `peso_senioridad` | 0.3 |
| `umbral_viabilidad` | 65 |

### Porcentaje de habilidades

- **Requeridas** = críticas ∪ deseables de la vacante, sin repetir. Las críticas y las deseables **pesan igual** (decisión tomada; ver [Decisiones](Design-Decisions)).
- Solo cuentan las habilidades del empleado en estado **AVALADO** (RN-20). Las que están en `AGREGADO` aparecen en el desglose como "sin avalar", pero no suman.
- Si la vacante no tiene habilidades requeridas, el porcentaje es 1.0 (no se divide entre cero).
- Las habilidades se comparan sin distinguir mayúsculas ("Docker" = "docker").

### Ajuste por senioridad

Niveles: `JUNIOR` (1) < `SEMI_SENIOR` (2) < `SENIOR` (3) < `LIDER` (4).

| Situación del empleado frente a la vacante | Factor |
|---|---|
| Mismo nivel | 1.0 |
| **Supera** el nivel pedido | 0.9 (`ajuste_senioridad_superior`) |
| 1 nivel por debajo | 0.6 |
| 2 niveles por debajo | 0.3 |
| 3 o más niveles por debajo | 0.0 |
| Sin senioridad declarada | Se toma como Junior |
| Vacante sin senioridad | 1.0 |

## Ejemplos

| Caso | Habilidades | Senioridad | Cálculo | Puntaje |
|---|---|---|---|---|
| Coincidencia total | 2 de 2 avaladas | Senior vs Senior | (1.0×0.7 + 1.0×0.3)×100 | **100.0** |
| Parcial | 1 de 2 | Senior vs Senior | (0.5×0.7 + 1.0×0.3)×100 | **65.0** (justo en el umbral: viable) |
| Sobrecalificado | 2 de 2 | Líder vs Senior | (1.0×0.7 + 0.9×0.3)×100 | **97.0** |
| Sin avalar | 1 de 2 (la otra está declarada pero sin aval) | Senior vs Senior | (0.5×0.7 + 1.0×0.3)×100 | **65.0** |
| Bajo | 0 de 1 | Senior vs Senior | (0×0.7 + 1.0×0.3)×100 | **30.0** (puede postularse, pero se le avisa) |

## Umbral y ranking (RN-27, RN-28, RN-14)

- **Puntaje ≥ 65:** el candidato es **viable** y aparece en el ranking del manager.
- **Puntaje < 65:** **sí puede postularse** (no se bloquea), pero se le avisa que sus posibilidades son bajas. No aparece en el ranking.
- **Ranking:** postulaciones ACTIVAS y viables, de mayor a menor puntaje. En un empate gana la que se postuló primero. **Nadie puede reordenarlo**: la API responde 409 a cualquier intento.
- **Indicador de color** (solo visual, no cambia el orden): verde ≥ 80, amarillo ≥ 70 y rojo por debajo de 70.
- **Ciclo de 3 días (RN-12):** el manager solo puede resolver las postulaciones que llegaron **antes del último corte**. Los cortes caen cada 3 días contados desde la activación de la vacante; las más nuevas esperan al siguiente ciclo.

## Dónde está el código

| Pieza | Clase |
|---|---|
| Fórmula pura (sin BD ni Spring) | `service/CalculadoraMatchScore` |
| Datos que entran (lista cerrada) | `CalculadoraMatchScore.Entrada`: habilidades avaladas, habilidades sin avalar, senioridad del empleado, habilidades requeridas y senioridad de la vacante. **No recibe el `Empleado`** |
| Cálculo + bitácora obligatoria | `service/MatchingService` (cada cálculo crea un registro en `bitacora_calculo`, con el detalle de la fórmula) |
| Recálculo cuando cambia un aval | `service/RecalculoService` |
| Ranking | `Vacante.calcularRanking` + `service/RankingService` |
| Parámetros | `config/AlgoritmoProperties` ← `conf/config.yaml` |

Pruebas: `CalculadoraMatchScoreTest` (14 casos) y `CalculadoraMatchScorePlanTest` (CP01 a CP10).
