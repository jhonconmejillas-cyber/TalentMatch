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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase asociativa Empleado - Habilidad (DC-01). Guarda el estado del aval (RN-19):
 * nace AGREGADO y solo el manager actual la pasa a AVALADO.
 */
@Entity
@Table(name = "habilidad_empleado")
public class HabilidadEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    @ManyToOne(optional = false)
    @JoinColumn(name = "habilidad_id")
    private Habilidad habilidad;
    @Enumerated(EnumType.STRING)
    private EstadoHabilidad estado = EstadoHabilidad.AGREGADO;
    private String nivelDeclarado;
    private LocalDate fechaAgregado;
    private LocalDateTime fechaAval;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avalado_por")
    private Manager avaladoPor;

    protected HabilidadEmpleado() {
    }

    public HabilidadEmpleado(Empleado empleado, Habilidad habilidad, String nivelDeclarado, LocalDate fechaAgregado) {
        this.empleado = empleado;
        this.habilidad = habilidad;
        this.nivelDeclarado = nivelDeclarado;
        this.fechaAgregado = fechaAgregado;
    }

    /** Pasa la habilidad a AVALADO y registra quien y cuando (RN-19). */
    public void avalar(Manager manager, LocalDateTime ahora) {
        if (estado == EstadoHabilidad.AVALADO) {
            throw new ReglaNegocioException("La habilidad ya esta avalada");
        }
        this.estado = EstadoHabilidad.AVALADO;
        this.avaladoPor = manager;
        this.fechaAval = ahora;
    }

    /** El manager no la avala: sigue visible en el perfil pero no cuenta (HU-007). */
    public void rechazar(Manager manager, LocalDateTime ahora) {
        if (estado != EstadoHabilidad.AGREGADO) {
            throw new ReglaNegocioException("Solo se puede rechazar una habilidad pendiente de aval");
        }
        this.estado = EstadoHabilidad.RECHAZADO;
        this.avaladoPor = manager;
        this.fechaAval = ahora;
    }

    /** RN-20: solo las avaladas entran al Puntaje Final. */
    public boolean cuentaParaCalculo() {
        return estado == EstadoHabilidad.AVALADO;
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

    public Habilidad getHabilidad() {
        return habilidad;
    }

    public EstadoHabilidad getEstado() {
        return estado;
    }

    public String getNivelDeclarado() {
        return nivelDeclarado;
    }

    public void setNivelDeclarado(String nivelDeclarado) {
        this.nivelDeclarado = nivelDeclarado;
    }

    public LocalDate getFechaAgregado() {
        return fechaAgregado;
    }

    public LocalDateTime getFechaAval() {
        return fechaAval;
    }

    public Manager getAvaladoPor() {
        return avaladoPor;
    }
}
