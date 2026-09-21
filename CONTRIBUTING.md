# Guia de contribucion — TalentMatch

## Flujo de trabajo

1. Cada integrante trabaja en su rama `features-<nombre>`.
2. Los cambios llegan a la rama principal mediante **pull request**.
3. Todo PR debe pasar las pruebas antes de fusionarse.

## Mensajes de commit

Se usa [Conventional Commits](https://www.conventionalcommits.org/es/):

```
feat(matching): calcular Match Score - closes #12
fix(vacante): respetar la activacion diferida de 24h
docs(reglas-negocio): actualizar RN-10 tras confirmacion con la empresa
```

## Antes de abrir un PR que toque la logica de negocio

Verifica que tu cambio sea consistente con la documentacion, **en este orden**:

1. [Reglas de Negocio](docs/reglas-negocio/RN-TalentMatch-Reglas-de-Negocio.md) — manda sobre todo lo demas.
2. [Requerimientos](docs/requerimientos/RF-RNF-TalentMatch.md)
3. [Casos de Uso](docs/casos-uso/CU-TalentMatch.md)

Si tu cambio **contradice** una regla de negocio, el PR no se fusiona: primero se actualiza la
regla, y con ella los requerimientos, las historias y los casos de uso afectados.

## Reglas que ninguna contribucion puede romper

| Regla | Donde se verifica |
|---|---|
| Solo las habilidades `avalado` entran al calculo (RN-20). | Prueba unitaria del Match Score. |
| Ningun dato personal interviene en el calculo (RNF-09). | Lista cerrada de campos en `backend/src/services/`. |
| Todo calculo queda en bitacora antes de devolverse (RNF-06). | CU_11, excepcion del paso 6. |
| El ranking nunca se reordena manualmente (RN-14). | Prueba de la API y de la UI. |
| La respuesta de rechazo no expone la identidad del seleccionado (RN-18). | Prueba automatica sobre el JSON de respuesta. |
