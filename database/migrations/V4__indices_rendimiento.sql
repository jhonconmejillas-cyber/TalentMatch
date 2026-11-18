-- =====================================================================
-- TalentMatch - V4: indices para el ranking y el calculo del Puntaje Final (HU-005, RNF-04)
-- =====================================================================

-- Ranking: postulaciones activas de una vacante ordenadas por Puntaje Final
CREATE INDEX ix_postulacion_ranking ON postulacion (vacante_id, estado, puntaje_final DESC);

-- Calculo: habilidades avaladas de un empleado (RN-20)
CREATE INDEX ix_habilidad_empleado_avaladas ON habilidad_empleado (empleado_id) WHERE estado = 'AVALADO';

-- Busqueda de vacantes activas y tareas programadas
CREATE INDEX ix_vacante_estado_activacion ON vacante (estado, fecha_activacion DESC);

-- Historial propio y reglas de postulacion
CREATE INDEX ix_postulacion_empleado_resolucion ON postulacion (empleado_id, estado, fecha_resolucion DESC);
