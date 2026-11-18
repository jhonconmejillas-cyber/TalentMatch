-- =====================================================================
-- TalentMatch - V2: distinguir el rechazo directo del manager (RN-29)
--
-- Cuando el manager acepta a un candidato, los demas quedan "no seleccionados"
-- (RECHAZADA) automaticamente. Para la alerta de rechazos repetidos solo cuentan
-- los rechazos que el manager hace DIRECTAMENTE sobre un candidato viable.
-- =====================================================================
ALTER TABLE postulacion ADD COLUMN rechazo_directo BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX ix_postulacion_rechazo_directo ON postulacion (vacante_id) WHERE rechazo_directo;
