package co.edu.javeriana.talentmatch.model;

import java.time.LocalDate;

/**
 * Clase asociativa Empleado - Proyecto (DC-01): el puesto que ocupa un empleado
 * en un proyecto durante un periodo. Al liberarse se genera una vacante (RN-02).
 */
public class AsignacionProyecto {

    private Long id;
    private Empleado empleado;
    private Proyecto proyecto;
    /** Nombre del puesto dentro del proyecto (ej. "Backend Developer"); titulo de la vacante que se genera. */
    private String cargo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private MotivoFinAsignacion motivoFin;

    protected AsignacionProyecto() {
    }

    public AsignacionProyecto(Empleado empleado, Proyecto proyecto, String cargo, LocalDate fechaInicio) {
        this.empleado = empleado;
        this.proyecto = proyecto;
        this.cargo = cargo;
        this.fechaInicio = fechaInicio;
    }

    /** El empleado deja el puesto (RN-02). */
    public void liberar(MotivoFinAsignacion motivo, LocalDate fecha) {
        if (motivo == null || fecha == null) {
            throw new ReglaNegocioException("Para liberar la asignacion se requiere el motivo y la fecha");
        }
        if (!estaVigente()) {
            throw new ReglaNegocioException("La asignacion ya fue liberada");
        }
        this.motivoFin = motivo;
        this.fechaFin = fecha;
    }

    public boolean estaVigente() {
        return fechaFin == null;
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

    public Proyecto getProyecto() {
        return proyecto;
    }

    public String getCargo() {
        return cargo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public MotivoFinAsignacion getMotivoFin() {
        return motivoFin;
    }
}
