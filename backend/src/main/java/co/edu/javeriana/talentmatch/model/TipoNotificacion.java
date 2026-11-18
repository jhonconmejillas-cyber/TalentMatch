package co.edu.javeriana.talentmatch.model;

public enum TipoNotificacion {
    /** RN-18: resultado anonimo al empleado rechazado. */
    RESULTADO_POSTULACION,
    /** RN-16: aviso a RR.HH. de una postulacion aceptada. */
    ROTACION_ACEPTADA,
    /** RN-22: confirmacion al empleado, con el aviso de incentivo. */
    ROTACION_CONFIRMADA,
    /** RF-10: se activo una vacante que cubre habilidades de la wishlist. */
    VACANTE_WISHLIST,
    /** RN-05: vacante sin match, para el manager y el jefe de area. */
    VACANTE_SIN_MATCH,
    /** Aviso de una alerta nueva para RR.HH. */
    ALERTA
}
