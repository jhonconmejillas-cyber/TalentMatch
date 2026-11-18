package co.edu.javeriana.talentmatch.model;

/** Ciclo de vida de una vacante (RN-02, RN-03). */
public enum EstadoVacante {
    BORRADOR,
    /** Nacio de una rotacion aceptada: espera la ventana de 24 h (RN-03). */
    PENDIENTE_ACTIVACION,
    /** Visible para los empleados y recibiendo postulaciones. */
    ACTIVA,
    /** Ya se acepto a un candidato. */
    CERRADA,
    /** El puesto se elimino o se cubrio fuera de la app (RN-05). */
    DESCARTADA
}
