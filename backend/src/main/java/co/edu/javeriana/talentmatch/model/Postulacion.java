package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Clase asociativa Empleado - Vacante (DC-01): la peticion de un empleado
 * para ocupar una vacante, con su puntaje y su estado (RN-06 ... RN-13).
 */
@Entity
@Table(name = "postulacion")
public class Postulacion {

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
    @Enumerated(EnumType.STRING)
    private EstadoPostulacion estado = EstadoPostulacion.ACTIVA;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;
    /** Posicion en el ranking al resolverse la vacante (HU-004: "Fuiste #1 de 12"). */
    private Integer posicionRanking;
    private Integer totalCandidatos;

    protected Postulacion() {
    }

    public Postulacion(Empleado empleado, Vacante vacante, LocalDateTime fechaCreacion) {
        this.empleado = empleado;
        this.vacante = vacante;
        this.fechaCreacion = fechaCreacion;
    }

    /** RN-25: Puntaje Final = Match Score. */
    public void registrarPuntaje(double matchScore) {
        this.matchScore = matchScore;
        this.puntajeFinal = matchScore;
    }

    /** RN-27: viable si el Puntaje Final alcanza el umbral. */
    public boolean esViable(double umbral) {
        return puntajeFinal >= umbral;
    }

    public void aceptar(LocalDateTime ahora) {
        resolver(EstadoPostulacion.ACEPTADA, ahora);
    }

    public void rechazar(LocalDateTime ahora) {
        resolver(EstadoPostulacion.RECHAZADA, ahora);
    }

    /** RN-09: el empleado acepto otra vacante. */
    public void cancelarPorRotacion(LocalDateTime ahora) {
        resolver(EstadoPostulacion.CANCELADA_POR_ROTACION, ahora);
    }

    public void registrarPosicion(int posicion, int total) {
        this.posicionRanking = posicion;
        this.totalCandidatos = total;
    }

    public boolean estaActiva() {
        return estado == EstadoPostulacion.ACTIVA;
    }

    private void resolver(EstadoPostulacion nuevo, LocalDateTime ahora) {
        if (estado != EstadoPostulacion.ACTIVA) {
            throw new ReglaNegocioException("La postulacion ya fue resuelta");
        }
        this.estado = nuevo;
        this.fechaResolucion = ahora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public Integer getPosicionRanking() {
        return posicionRanking;
    }

    public Integer getTotalCandidatos() {
        return totalCandidatos;
    }
}
