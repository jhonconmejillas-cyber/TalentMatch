package co.edu.javeriana.talentmatch.model;

import java.time.LocalDateTime;

/** Analista de RR.HH. (DC-01): atiende alertas (RN-29, RN-30) y configura umbrales. */
public class AnalistaRRHH extends Usuario {

    protected AnalistaRRHH() {
    }

    public AnalistaRRHH(String nombre, String correo, String contrasenaHash) {
        super(nombre, correo, contrasenaHash);
    }

    @Override
    public Rol getRol() {
        return Rol.RRHH;
    }

    public void revisarAlerta(Alerta alerta, LocalDateTime ahora) {
        alerta.resolver(this, ahora);
    }
}
