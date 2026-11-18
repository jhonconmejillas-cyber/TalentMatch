package co.edu.javeriana.talentmatch.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Puesto abierto en un proyecto (DC-01). Puede publicarla un manager (RF-11)
 * o generarla el sistema al liberarse una asignacion (RN-02).
 */
public class Vacante {

    private Long id;
    private String titulo;
    private String descripcion;
    private Senioridad senioridad;
    private String area;
    private EstadoVacante estado = EstadoVacante.BORRADOR;
    private OrigenVacante origen = OrigenVacante.MANUAL;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActivacion;
    private LocalDateTime fechaCierre;
    private Manager manager;
    private Proyecto proyecto;
    private Set<Habilidad> habilidadesCriticas = new HashSet<>();
    private Set<Habilidad> habilidadesDeseables = new HashSet<>();

    protected Vacante() {
    }

    public Vacante(String titulo, Senioridad senioridad, String area, Manager manager, LocalDateTime fechaCreacion) {
        this.titulo = titulo;
        this.senioridad = senioridad;
        this.area = area;
        this.manager = manager;
        this.fechaCreacion = fechaCreacion;
    }

    /** La vacante queda visible y recibe postulaciones. */
    public void activar(LocalDateTime ahora) {
        if (estado != EstadoVacante.BORRADOR && estado != EstadoVacante.PENDIENTE_ACTIVACION) {
            throw new ReglaNegocioException("Solo se puede activar una vacante en borrador o pendiente de activacion");
        }
        this.estado = EstadoVacante.ACTIVA;
        this.fechaActivacion = ahora;
    }

    /** RN-03: la vacante espera hasta {@code fecha} antes de ser visible. */
    public void programarActivacion(LocalDateTime fecha) {
        if (estado != EstadoVacante.BORRADOR) {
            throw new ReglaNegocioException("Solo se puede programar la activacion de una vacante en borrador");
        }
        this.estado = EstadoVacante.PENDIENTE_ACTIVACION;
        this.fechaActivacion = fecha;
    }

    /** RN-03: true si ya paso la ventana de activacion diferida. */
    public boolean debeActivarse(LocalDateTime ahora) {
        return estado == EstadoVacante.PENDIENTE_ACTIVACION
                && fechaActivacion != null && !ahora.isBefore(fechaActivacion);
    }

    /** Se acepto a un candidato: la vacante deja de recibir postulaciones. */
    public void cerrar(LocalDateTime ahora) {
        if (estado != EstadoVacante.ACTIVA) {
            throw new ReglaNegocioException("Solo se puede cerrar una vacante activa");
        }
        this.estado = EstadoVacante.CERRADA;
        this.fechaCierre = ahora;
    }

    public void descartar(LocalDateTime ahora) {
        if (estado == EstadoVacante.CERRADA) {
            throw new ReglaNegocioException("Una vacante cerrada no se puede descartar");
        }
        this.estado = EstadoVacante.DESCARTADA;
        this.fechaCierre = ahora;
    }

    public boolean estaActiva() {
        return estado == EstadoVacante.ACTIVA;
    }

    /**
     * RN-10: dos vacantes son "el mismo puesto" si son del mismo proyecto y tienen el mismo titulo.
     * Una vacante distinta del mismo puesto es una "nueva rotacion" en el.
     */
    public boolean esMismoPuestoQue(Vacante otra) {
        if (otra == null || titulo == null || !titulo.equalsIgnoreCase(otra.titulo)) {
            return false;
        }
        if (proyecto == null || otra.proyecto == null) {
            return proyecto == otra.proyecto && area != null && area.equalsIgnoreCase(otra.area);
        }
        return proyecto.getId() != null && proyecto.getId().equals(otra.proyecto.getId());
    }

    /** Criticas + deseables, sin repetir. */
    public Set<Habilidad> habilidadesRequeridas() {
        Set<Habilidad> todas = new LinkedHashSet<>(habilidadesCriticas);
        todas.addAll(habilidadesDeseables);
        return todas;
    }

    /**
     * Ranking del diagrama de clases (RN-14, RN-27): solo postulaciones activas con
     * Puntaje Final mayor o igual al umbral, de mayor a menor. Ante empate se ordena por
     * fecha de postulacion para que el orden sea siempre el mismo.
     */
    public static List<Postulacion> calcularRanking(List<Postulacion> postulaciones, double umbral) {
        return postulaciones.stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.ACTIVA)
                .filter(p -> p.esViable(umbral))
                .sorted(Comparator.comparingDouble(Postulacion::getPuntajeFinal).reversed()
                        .thenComparing(Postulacion::getFechaCreacion, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Senioridad getSenioridad() {
        return senioridad;
    }

    public void setSenioridad(Senioridad senioridad) {
        this.senioridad = senioridad;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public EstadoVacante getEstado() {
        return estado;
    }

    public OrigenVacante getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenVacante origen) {
        this.origen = origen;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActivacion() {
        return fechaActivacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Set<Habilidad> getHabilidadesCriticas() {
        return Set.copyOf(habilidadesCriticas);
    }

    public void setHabilidadesCriticas(Set<Habilidad> habilidades) {
        this.habilidadesCriticas = new HashSet<>(habilidades);
    }

    public Set<Habilidad> getHabilidadesDeseables() {
        return Set.copyOf(habilidadesDeseables);
    }

    public void setHabilidadesDeseables(Set<Habilidad> habilidades) {
        this.habilidadesDeseables = new HashSet<>(habilidades);
    }
}
