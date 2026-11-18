package co.edu.javeriana.talentmatch.model;

/** Por que existe una vacante (RN-02). Define si se activa de inmediato o a las 24 h (RN-03). */
public enum OrigenVacante {
    /** Publicada por un manager (RF-11). */
    MANUAL,
    /** Un empleado renuncio. */
    RENUNCIA,
    /** El ocupante acepto otra vacante: se activa 24 h despues (RN-03). */
    ROTACION_ACEPTADA,
    /** El proyecto se cerro o se reorganizo. */
    CAMBIO_PROYECTO;

    /** RN-03: solo la vacante que nace de una rotacion aceptada espera antes de activarse. */
    public boolean requiereActivacionDiferida() {
        return this == ROTACION_ACEPTADA;
    }
}
