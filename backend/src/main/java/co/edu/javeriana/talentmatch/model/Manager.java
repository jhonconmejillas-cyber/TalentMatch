package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

/**
 * Manager / Lider Tecnico (DC-01). Hereda de Empleado: tambien puede rotar (RN-11).
 * Avala las habilidades de su equipo y decide sobre el ranking de sus vacantes.
 *
 * Publicar vacantes y revisar el ranking necesitan repositorios, asi que viven en la
 * capa Service; aqui quedan las acciones que se resuelven con los objetos en memoria.
 */
@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Empleado {

    protected Manager() {
    }

    public Manager(String nombre, String correo, String contrasenaHash) {
        super(nombre, correo, contrasenaHash);
    }

    @Override
    public Rol getRol() {
        return Rol.MANAGER;
    }

    /** RN-19: solo el manager actual del empleado puede avalar sus habilidades. */
    public void avalarHabilidad(HabilidadEmpleado habilidad, LocalDateTime ahora) {
        verificarQueEsSuEquipo(habilidad);
        habilidad.avalar(this, ahora);
    }

    public void rechazarHabilidad(HabilidadEmpleado habilidad, LocalDateTime ahora) {
        verificarQueEsSuEquipo(habilidad);
        habilidad.rechazar(this, ahora);
    }

    public void aceptarCandidato(Postulacion postulacion, LocalDateTime ahora) {
        verificarQueEsSuVacante(postulacion);
        postulacion.aceptar(ahora);
    }

    public void rechazarCandidato(Postulacion postulacion, LocalDateTime ahora) {
        verificarQueEsSuVacante(postulacion);
        postulacion.rechazarDirectamente(ahora);
    }

    private void verificarQueEsSuEquipo(HabilidadEmpleado habilidad) {
        if (!habilidad.getEmpleado().esManejadoPor(this)) {
            throw new ReglaNegocioException("Solo el manager actual del empleado puede revisar sus habilidades");
        }
    }

    private void verificarQueEsSuVacante(Postulacion postulacion) {
        if (!mismoUsuarioQue(postulacion.getVacante().getManager())) {
            throw new ReglaNegocioException("Solo el manager que publico la vacante puede decidir sobre ella");
        }
    }
}
