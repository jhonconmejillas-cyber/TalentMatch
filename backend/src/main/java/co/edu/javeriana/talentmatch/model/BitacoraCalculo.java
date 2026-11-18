package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Registro de un calculo de Puntaje Final (RNF-06). Es de solo lectura:
 * todos los datos se fijan en el constructor y no hay setters publicos.
 */
@Entity
@Table(name = "bitacora_calculo")
// @Immutable: Hibernate nunca genera UPDATE para esta entidad. Ademas, un trigger
// de la base de datos rechaza UPDATE y DELETE (V1__esquema_inicial.sql).
@Immutable
public class BitacoraCalculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacante_id")
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
