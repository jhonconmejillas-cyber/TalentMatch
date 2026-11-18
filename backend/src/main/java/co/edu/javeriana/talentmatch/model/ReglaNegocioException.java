package co.edu.javeriana.talentmatch.model;

/**
 * Se lanza cuando una operacion viola una regla de negocio
 * (por ejemplo, avalar una habilidad ya avalada). El mensaje va en espanol
 * porque llega tal cual al usuario (RNF-13).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
