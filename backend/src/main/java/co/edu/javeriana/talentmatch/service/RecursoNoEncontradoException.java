package co.edu.javeriana.talentmatch.service;

/** El recurso pedido no existe (se responde 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
