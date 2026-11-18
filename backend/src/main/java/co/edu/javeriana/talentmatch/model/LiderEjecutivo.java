package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Lider Ejecutivo (DC-01): consulta el reporte top 10 (RN-21), que se calcula en la capa Service. */
@Entity
@DiscriminatorValue("LIDER_EJECUTIVO")
public class LiderEjecutivo extends Usuario {

    protected LiderEjecutivo() {
    }

    public LiderEjecutivo(String nombre, String correo, String contrasenaHash) {
        super(nombre, correo, contrasenaHash);
    }

    @Override
    public Rol getRol() {
        return Rol.LIDER_EJECUTIVO;
    }
}
