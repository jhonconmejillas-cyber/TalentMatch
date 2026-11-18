package co.edu.javeriana.talentmatch.model;

import java.time.LocalDateTime;

/** Alerta para RR.HH.: rechazos repetidos (RN-29) o vacante sin match (RN-30). */
public class Alerta {

    private Long id;
    private TipoAlerta tipo;
    private EstadoAlerta estado = EstadoAlerta.ABIERTA;
    private LocalDateTime fecha;
    /** Manager involucrado (quien rechazo o quien solicito la vacante). */
    private Manager manager;
    private Vacante vacante;
    private String detalle;
    private AnalistaRRHH atendidaPor;
    private LocalDateTime fechaResolucion;

    protected Alerta() {
    }

    public Alerta(TipoAlerta tipo, Manager manager, Vacante vacante, String detalle, LocalDateTime fecha) {
        this.tipo = tipo;
        this.manager = manager;
        this.vacante = vacante;
        this.detalle = detalle;
        this.fecha = fecha;
    }

    public void resolver(AnalistaRRHH analista, LocalDateTime ahora) {
        if (estado == EstadoAlerta.RESUELTA) {
            throw new ReglaNegocioException("La alerta ya fue resuelta");
        }
        this.estado = EstadoAlerta.RESUELTA;
        this.atendidaPor = analista;
        this.fechaResolucion = ahora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Manager getManager() {
        return manager;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public String getDetalle() {
        return detalle;
    }

    public AnalistaRRHH getAtendidaPor() {
        return atendidaPor;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }
}
