package co.edu.javeriana.talentmatch.model;

/**
 * Estados de una habilidad en el perfil del empleado (RN-19).
 * Solo {@link #AVALADO} entra al calculo del Puntaje Final (RN-20).
 */
public enum EstadoHabilidad {
    /** Autoreportada por el empleado; visible en el perfil pero no cuenta. */
    AGREGADO,
    /** Confirmada por el manager actual; es la unica que cuenta. */
    AVALADO,
    /** El manager la reviso y no la avalo; sigue sin contar. */
    RECHAZADO
}
