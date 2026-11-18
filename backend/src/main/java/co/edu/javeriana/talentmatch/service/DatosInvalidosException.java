package co.edu.javeriana.talentmatch.service;

import java.util.Map;

/** Un dato de entrada no cumple una regla; indica el campo y como corregirlo (RNF-13). Se responde 400. */
public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> campos;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.campos = Map.of(campo, mensaje);
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
