package co.edu.javeriana.talentmatch.model;

/** Estados de una postulacion (RN-09). */
public enum EstadoPostulacion {
    ACTIVA,
    ACEPTADA,
    RECHAZADA,
    /** El empleado acepto otra vacante (RN-09). */
    CANCELADA_POR_ROTACION
}
