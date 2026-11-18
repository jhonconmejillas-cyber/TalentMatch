package co.edu.javeriana.talentmatch.service;

/**
 * El usuario tiene sesion y el rol correcto, pero no relacion con el recurso
 * (ej. un manager que no es el manager actual del empleado). Se responde 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
