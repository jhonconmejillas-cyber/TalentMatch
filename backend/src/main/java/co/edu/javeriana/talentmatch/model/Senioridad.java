package co.edu.javeriana.talentmatch.model;

/**
 * Niveles de senioridad, de menor a mayor. El {@link #nivel()} permite comparar
 * al empleado con la vacante para el ajuste por senioridad del Match Score (RN-23).
 */
public enum Senioridad {
    JUNIOR(1),
    SEMI_SENIOR(2),
    SENIOR(3),
    LIDER(4);

    private final int nivel;

    Senioridad(int nivel) {
        this.nivel = nivel;
    }

    public int nivel() {
        return nivel;
    }

    /** Diferencia de niveles: positiva si este nivel supera al otro. */
    public int diferenciaCon(Senioridad otra) {
        return this.nivel - otra.nivel;
    }
}
