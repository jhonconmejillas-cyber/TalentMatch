package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Proyecto de la empresa al que se asignan empleados y del que nacen vacantes (RN-02). */
@Entity
@Table(name = "proyecto")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String area;
    @Enumerated(EnumType.STRING)
    private EstadoProyecto estado = EstadoProyecto.ACTIVO;

    protected Proyecto() {
    }

    public Proyecto(String nombre, String area) {
        this.nombre = nombre;
        this.area = area;
    }

    public void cerrar() {
        this.estado = EstadoProyecto.CERRADO;
    }

    public boolean estaActivo() {
        return estado == EstadoProyecto.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }
}
