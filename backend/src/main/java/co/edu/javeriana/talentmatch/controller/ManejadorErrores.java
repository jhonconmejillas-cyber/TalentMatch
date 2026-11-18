package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.model.ReglaNegocioException;
import co.edu.javeriana.talentmatch.security.RegistroAccesosDenegados;
import co.edu.javeriana.talentmatch.service.AccesoDenegadoException;
import co.edu.javeriana.talentmatch.service.CredencialesInvalidasException;
import co.edu.javeriana.talentmatch.service.DatosInvalidosException;
import co.edu.javeriana.talentmatch.service.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte las excepciones en respuestas JSON con un mensaje en espanol (RNF-13):
 * { "mensaje": "...", "campos": { "correo": "..." } }
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    private final RegistroAccesosDenegados accesosDenegados;

    public ManejadorErrores(RegistroAccesosDenegados accesosDenegados) {
        this.accesosDenegados = accesosDenegados;
    }

    /** Errores de @Valid en los DTO: se indica cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Revisa los campos marcados.", campos);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(DatosInvalidosException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCampos());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> formato(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El formato de los datos enviados no es válido.", null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> credenciales(CredencialesInvalidasException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage(), null);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccesoDenegadoException e, HttpServletRequest req) {
        accesosDenegados.registrar(req.getMethod() + " " + req.getRequestURI(), e.getMessage());
        return respuesta(HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    /** Una regla de negocio impide la operacion (ej. avalar dos veces). */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> reglaNegocio(ReglaNegocioException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    private static ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje,
                                                                 Map<String, String> campos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", mensaje);
        if (campos != null && !campos.isEmpty()) {
            cuerpo.put("campos", campos);
        }
        if (estado.is5xxServerError()) {
            LOG.error(mensaje);
        }
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
