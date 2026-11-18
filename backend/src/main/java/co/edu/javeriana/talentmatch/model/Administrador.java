package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Administrador del Sistema (DC-01): gestiona cuentas y roles (RF-03). */
@Entity
@DiscriminatorValue("ADMIN")
public class Administrador extends Usuario {

    protected Administrador() {
    }

    public Administrador(String nombre, String correo, String contrasenaHash) {
        super(nombre, correo, contrasenaHash);
    }

    @Override
    public Rol getRol() {
        return Rol.ADMIN;
    }

    /** Activa o desactiva una cuenta. El cambio de rol se hace en la capa Service. */
    public void gestionarUsuario(Usuario usuario, boolean activo) {
        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
    }
}
