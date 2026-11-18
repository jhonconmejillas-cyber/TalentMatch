package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Locale;
import java.util.Objects;

/** Habilidad del catalogo (Java, Docker, Kubernetes...). */
@Entity
@Table(name = "habilidad")
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    protected Habilidad() {
    }

    public Habilidad(String nombre) {
        this.nombre = nombre;
    }

    public Habilidad(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    // Dos habilidades son la misma si tienen el mismo nombre, sin importar
    // mayusculas ("docker" == "Docker"). El nombre es unico en el catalogo.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Habilidad otra)) {
            return false;
        }
        return nombre != null && otra.nombre != null && nombre.equalsIgnoreCase(otra.nombre);
    }

    @Override
    public int hashCode() {
        return nombre == null ? 0 : Objects.hash(nombre.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return nombre;
    }
}
