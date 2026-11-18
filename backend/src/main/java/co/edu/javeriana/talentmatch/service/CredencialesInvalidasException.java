package co.edu.javeriana.talentmatch.service;

/** Correo o contrasena incorrectos, o cuenta desactivada (se responde 401). */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
