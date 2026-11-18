package co.edu.javeriana.talentmatch.model;

/** Por que un empleado deja una asignacion; cada motivo genera una vacante (RN-02). */
public enum MotivoFinAsignacion {
    RENUNCIA,
    ROTACION_ACEPTADA,
    CIERRE_PROYECTO;

    /** Origen de la vacante que se genera al liberar el puesto. */
    public OrigenVacante origenVacante() {
        return switch (this) {
            case RENUNCIA -> OrigenVacante.RENUNCIA;
            case ROTACION_ACEPTADA -> OrigenVacante.ROTACION_ACEPTADA;
            case CIERRE_PROYECTO -> OrigenVacante.CAMBIO_PROYECTO;
        };
    }
}
