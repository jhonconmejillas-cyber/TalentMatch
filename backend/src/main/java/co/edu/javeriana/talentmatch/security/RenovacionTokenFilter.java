package co.edu.javeriana.talentmatch.security;

import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Expiracion por INACTIVIDAD (HU-006, escenario 3): el token dura los minutos configurados
 * (30). Cada peticion autenticada recibe un token nuevo en el encabezado {@link #ENCABEZADO},
 * asi la sesion se mantiene mientras el usuario trabaja y se cierra sola tras 30 minutos sin actividad.
 * Si la cuenta fue desactivada, el token deja de servir aunque no haya vencido.
 */
public class RenovacionTokenFilter extends OncePerRequestFilter {

    public static final String ENCABEZADO = "X-Token-Renovado";

    private final TokenService tokens;
    private final UsuarioRepository usuarios;
    private final RespuestasSeguridad respuestas;

    public RenovacionTokenFilter(TokenService tokens, UsuarioRepository usuarios, RespuestasSeguridad respuestas) {
        this.tokens = tokens;
        this.usuarios = usuarios;
        this.respuestas = respuestas;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            var usuario = usuarios.findById(Long.valueOf(jwt.getSubject())).filter(u -> u.isActivo());
            if (usuario.isEmpty()) {
                SecurityContextHolder.clearContext();
                respuestas.commence(request, response, null);
                return;
            }
            response.setHeader(ENCABEZADO, tokens.emitir(usuario.get()).token());
        }
        cadena.doFilter(request, response);
    }
}
