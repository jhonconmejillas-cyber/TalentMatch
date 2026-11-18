package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
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
@Entity
@DiscriminatorValue("EMPLEADO")
// Los datos profesionales estan en la tabla "empleado", unida a "usuario" por el mismo id.
@SecondaryTable(name = "empleado", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class Empleado extends Usuario {

    @Enumerated(EnumType.STRING)
    @Column(table = "empleado")
    private Senioridad senioridad;
    @Column(table = "empleado")
    private int aniosExperiencia;
    @Column(table = "empleado")
    private String area;
    /** Inicio en el rol actual: determina la antiguedad minima de 6 meses (RN-07). */
    @Column(table = "empleado")
    private LocalDate fechaInicioRol;
    /** Ingreso a la empresa: criterio "anios en la empresa" del reporte top 10 (RN-21). */
    @Column(table = "empleado")
    private LocalDate fechaIngresoEmpresa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", table = "empleado")
    private Manager managerActual;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HabilidadEmpleado> habilidades = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "empleado_id"),
            inverseJoinColumns = @JoinColumn(name = "habilidad_id"))
    private Set<Habilidad> wishlist = new HashSet<>();

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL)
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
