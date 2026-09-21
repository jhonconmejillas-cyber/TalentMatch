# Documentación de TalentMatch

Esta carpeta contiene **toda la especificación del proyecto**: reglas de negocio, requerimientos,
historias de usuario y casos de uso.

## Índice

| Carpeta | Documento | Qué contiene | IDs |
|---|---|---|---|
| [`reglas-negocio/`](reglas-negocio/) | [RN-TalentMatch-Reglas-de-Negocio.md](reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) | Reglas de negocio del modelo de rotación continuo. **Fuente única de verdad.** | RN-01 … RN-30, PEN-01 … PEN-03 |
| [`requerimientos/`](requerimientos/) | [RF-RNF-TalentMatch.md](requerimientos/RF-RNF-TalentMatch.md) | Requerimientos funcionales y no funcionales. | RF-01 … RF-33, RNF-01 … RNF-13 |
| [`historias-usuario/`](historias-usuario/) | [HU-TalentMatch.md](historias-usuario/HU-TalentMatch.md) | Historias de usuario con criterios de aceptación, Definición de Terminado y subtareas técnicas. | HU-001 … HU-010 |
| [`casos-uso/`](casos-uso/) | [CU-TalentMatch.md](casos-uso/CU-TalentMatch.md) | Casos de uso en formato estándar (secuencia normal, excepciones, pre y postcondición). | CU_01 … CU_25 |
| [`diagramas/`](diagramas/) | — | Diagramas de casos de uso, de clases y de despliegue. | — |
| [`wiki/`](wiki/) | — | Copia versionada de las páginas del wiki del repositorio. | — |

## Jerarquía y orden de precedencia

```
                    REGLAS DE NEGOCIO (RN)
                 fuente unica de verdad · manda
                             |
             +---------------+---------------+
             |                               |
    REQUERIMIENTOS (RF/RNF)        HISTORIAS DE USUARIO (HU)
      que debe hacer / como           el valor para el usuario
             |                               |
             +---------------+---------------+
                             |
                      CASOS DE USO (CU)
                como ocurre paso a paso, con excepciones
```

**Si dos documentos se contradicen, prevalecen las Reglas de Negocio.**

## Cómo mantenerla

1. Todo cambio de alcance empieza **modificando las Reglas de Negocio**.
2. Luego se propaga a los requerimientos afectados (columna `RN` de cada tabla).
3. Luego a las historias de usuario y a los casos de uso que los realizan.
4. Las matrices de trazabilidad de
   [Casos de Uso §4](casos-uso/CU-TalentMatch.md#4-matriz-de-trazabilidad) deben quedar completas:
   **ningún RF sin CU, ningún CU sin RF.**
5. Si la página equivalente existe en el wiki, se actualiza también la copia de [`wiki/`](wiki/).

## Estado de la especificación

| Métrica | Valor |
|---|---|
| Reglas de negocio | 30 |
| Requerimientos funcionales | 33 |
| Requerimientos no funcionales | 13 |
| Historias de usuario | 10 |
| Casos de uso | 25 |
| Cobertura RF → CU | **100 %** (33 de 33) |
| Pendientes por confirmar con la empresa | 3 (PEN-01, PEN-02, PEN-03) |
