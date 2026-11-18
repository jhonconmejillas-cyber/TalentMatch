package co.edu.javeriana.talentmatch.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Ingeniero de la organizacion (DC-01). Mantiene su perfil, agrega habilidades
 * y se postula a vacantes. {@link Manager} hereda de esta clase porque todo manager
 * tambien es un empleado que puede rotar (RN-11).
 */
public class Empleado extends Usuario {

    private Senioridad senioridad;
    private int aniosExperiencia;
    private String area;
    /** Inicio en el rol actual: determina la antiguedad minima de 6 meses (RN-07). */
    private LocalDate fechaInicioRol;
    /** Ingreso a la empresa: criterio "anios en la empresa" del reporte top 10 (RN-21). */
    private LocalDate fechaIngresoEmpresa;
    private Manager managerActual;

    private List<HabilidadEmpleado> habilidades = new ArrayList<>();
    private Set<Habilidad> wishlist = new HashSet<>();
    private List<AsignacionProyecto> asignaciones = new ArrayList<>();

    protected Empleado() {
    }

    public Empleado(String nombre, String correo, String contrasenaHash) {
        super(nombre, correo, contrasenaHash);
    }

    @Override
    public Rol getRol() {
        return Rol.EMPLEADO;
    }

    /**
     * RN-19: toda habilidad nueva nace en estado AGREGADO.
     * Si el empleado ya la tenia en su perfil, no se duplica.
     */
    public HabilidadEmpleado agregarHabilidad(Habilidad habilidad, String nivelDeclarado, LocalDate hoy) {
        if (tieneHabilidad(habilidad)) {
            throw new ReglaNegocioException("La habilidad " + habilidad.getNombre() + " ya esta en tu perfil");
        }
        HabilidadEmpleado nueva = new HabilidadEmpleado(this, habilidad, nivelDeclarado, hoy);
        habilidades.add(nueva);
        return nueva;
    }

    public boolean tieneHabilidad(Habilidad habilidad) {
        return habilidades.stream().anyMatch(h -> h.getHabilidad().equals(habilidad));
    }

    /** RN-20: solo las habilidades avaladas cuentan para el calculo. */
    public List<Habilidad> habilidadesAvaladas() {
        return habilidades.stream()
                .filter(HabilidadEmpleado::cuentaParaCalculo)
                .map(HabilidadEmpleado::getHabilidad)
                .toList();
    }

    public void agregarAWishlist(Habilidad habilidad) {
        wishlist.add(habilidad);
    }

    public void quitarDeWishlist(Habilidad habilidad) {
        wishlist.remove(habilidad);
    }

    /** La asignacion vigente (sin fecha de fin), si existe. */
    public Optional<AsignacionProyecto> asignacionActual() {
        return asignaciones.stream().filter(AsignacionProyecto::estaVigente).findFirst();
    }

    public void agregarAsignacion(AsignacionProyecto asignacion) {
        if (asignacion.getEmpleado() != this && !mismoUsuarioQue(asignacion.getEmpleado())) {
            throw new ReglaNegocioException("La asignacion pertenece a otro empleado");
        }
        if (asignacion.estaVigente() && asignacionActual().isPresent()) {
            throw new ReglaNegocioException("El empleado ya tiene una asignacion vigente: liberala antes de asignar otra");
        }
        asignaciones.add(asignacion);
    }

    /** RN-07: fecha a partir de la cual cumple la antiguedad minima en su rol. */
    public LocalDate fechaHabilitacionPorAntiguedad(int mesesMinimos) {
        return fechaInicioRol == null ? null : fechaInicioRol.plusMonths(mesesMinimos);
    }

    /** True si {@code manager} es el manager actual de este empleado (RN-19). */
    public boolean esManejadoPor(Manager manager) {
        return managerActual != null && managerActual.mismoUsuarioQue(manager);
    }

    public Senioridad getSenioridad() {
        return senioridad;
    }

    public void setSenioridad(Senioridad senioridad) {
        this.senioridad = senioridad;
    }

    public int getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(int aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public LocalDate getFechaInicioRol() {
        return fechaInicioRol;
    }

    public void setFechaInicioRol(LocalDate fechaInicioRol) {
        this.fechaInicioRol = fechaInicioRol;
    }

    public LocalDate getFechaIngresoEmpresa() {
        return fechaIngresoEmpresa;
    }

    public void setFechaIngresoEmpresa(LocalDate fechaIngresoEmpresa) {
        this.fechaIngresoEmpresa = fechaIngresoEmpresa;
    }

    public Manager getManagerActual() {
        return managerActual;
    }

    public void setManagerActual(Manager managerActual) {
        this.managerActual = managerActual;
    }

    /** Copia de solo lectura: las habilidades se agregan con {@link #agregarHabilidad}. */
    public List<HabilidadEmpleado> getHabilidades() {
        return List.copyOf(habilidades);
    }

    public Set<Habilidad> getWishlist() {
        return Set.copyOf(wishlist);
    }

    public List<AsignacionProyecto> getAsignaciones() {
        return List.copyOf(asignaciones);
    }
}
