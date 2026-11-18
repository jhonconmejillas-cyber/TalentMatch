package co.edu.javeriana.talentmatch.model;

import java.time.LocalDateTime;

/**
 * Registro de un calculo de Puntaje Final (RNF-06). Es de solo lectura:
 * todos los datos se fijan en el constructor y no hay setters publicos.
 */
public class BitacoraCalculo {

    private Long id;
    private Empleado empleado;
    private Vacante vacante;
    private double matchScore;
    private double puntajeFinal;
    private double umbralAplicado;
    private LocalDateTime fechaHora;
    /** Desglose del calculo en texto (habilidades coincidentes, ajuste de senioridad, pesos). */
    private String detalle;

    protected BitacoraCalculo() {
    }

    public BitacoraCalculo(Empleado empleado, Vacante vacante, double matchScore, double puntajeFinal,
                           double umbralAplicado, LocalDateTime fechaHora, String detalle) {
        this.empleado = empleado;
        this.vacante = vacante;
        this.matchScore = matchScore;
        this.puntajeFinal = puntajeFinal;
        this.umbralAplicado = umbralAplicado;
        this.fechaHora = fechaHora;
        this.detalle = detalle;
    }

    public Long getId() {
        return id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public double getPuntajeFinal() {
        return puntajeFinal;
    }

    public double getUmbralAplicado() {
        return umbralAplicado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getDetalle() {
        return detalle;
    }
}
