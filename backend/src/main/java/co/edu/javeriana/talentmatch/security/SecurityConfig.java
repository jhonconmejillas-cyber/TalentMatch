package co.edu.javeriana.talentmatch.security;

import co.edu.javeriana.talentmatch.config.CorsProperties;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API (RNF-07):
 * - Sin sesion de servidor: cada peticion trae su token en "Authorization: Bearer ...".
 * - El token lleva el rol del usuario; Spring lo convierte en la autoridad ROLE_<rol>.
 * - La MATRIZ DE PERMISOS por ruta esta en {@link #filterChain}; las reglas que dependen
 *   del recurso (ej. "solo el manager actual") se verifican en la capa Service.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RespuestasSeguridad respuestas, TokenService tokens,
                                           UsuarioRepository usuarios) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API sin cookies: el token viaja en un encabezado
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        // Rutas publicas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/health", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Matriz de permisos por rol (RNF-07, HU-006)
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/alertas/**", "/api/v1/asignaciones/**", "/api/v1/gestion/**",
                                "/api/v1/reportes/rechazos/**").hasRole("RRHH")
                        // Cualquier otra ruta exige sesion; el permiso fino va en cada controlador
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorRoles()))
                        .authenticationEntryPoint(respuestas))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                // HU-006: renueva el token en cada peticion (expira tras 30 min de INACTIVIDAD)
                .addFilterAfter(new RenovacionTokenFilter(tokens, usuarios, respuestas),
                        BearerTokenAuthenticationFilter.class)
                // HTTPS (RNF-07): el navegador recuerda usar solo HTTPS por un anio (HSTS).
                // Spring solo envia este encabezado en respuestas que ya viajan por HTTPS.
                .headers(h -> h.httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000)));
        return http.build();
    }

    /** Lee el claim "rol" del token y lo convierte en ROLE_EMPLEADO, ROLE_MANAGER, etc. */
    private JwtAuthenticationConverter convertidorRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(roles);
        return convertidor;
    }

    @Bean
    public JwtDecoder jwtDecoder(TokenService tokens) {
        return tokens.decoder();
    }

    /** Contrasenas con hash BCrypt: nunca se guardan en texto plano (RNF-07). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS: por seguridad, el navegador bloquea que una pagina (ej. el frontend en
     * http://localhost:5500) llame con fetch a OTRO origen (la API en http://localhost:8080),
     * a menos que la API responda "a ese origen si le permito".
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties cors) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(cors.origenesPermitidos());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Authorization -> el token del login; Content-Type -> el cuerpo va en JSON
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // El frontend debe poder leer el token renovado (expiracion por inactividad, HU-006)
        config.setExposedHeaders(List.of(RenovacionTokenFilter.ENCABEZADO, "Content-Disposition"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
