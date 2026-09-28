package co.edu.javeriana.talentmatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: por seguridad, el navegador bloquea que una pagina (ej. el frontend en
 * http://localhost:5500) llame con fetch a OTRO origen (la API en http://localhost:8080),
 * a menos que la API responda "a ese origen si le permito".
 * Esta clase le dice a Spring a que origenes responder.
 */
@Configuration // Clase de configuracion: Spring la carga al arrancar
public class CorsConfig implements WebMvcConfigurer { // WebMvcConfigurer permite ajustar el comportamiento web de Spring

    private final CorsProperties cors;

    // Inyeccion de dependencias: Spring nos entrega CorsProperties
    // ya lleno con los valores de application.properties.
    public CorsConfig(CorsProperties cors) {
        this.cors = cors;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica a TODAS las rutas que empiezan por /api/
                // Solo estos origenes pueden llamar; cualquier otra pagina queda bloqueada.
                // toArray convierte la List<String> en String[], que es lo que pide el metodo.
                .allowedOrigins(cors.origenesPermitidos().toArray(String[]::new))
                // Metodos HTTP permitidos. OPTIONS es la "pregunta previa" que hace
                // el navegador antes de un PATCH/PUT/DELETE para confirmar el permiso.
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // Encabezados que el frontend puede enviar:
                //   Authorization -> el token del login (issue #19)
                //   Content-Type  -> indica que el cuerpo va en JSON
                .allowedHeaders("Authorization", "Content-Type");
    }
}