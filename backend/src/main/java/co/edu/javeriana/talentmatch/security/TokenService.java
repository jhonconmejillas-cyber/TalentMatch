package co.edu.javeriana.talentmatch.security;

import co.edu.javeriana.talentmatch.config.SeguridadProperties;
import co.edu.javeriana.talentmatch.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/**
 * Emite y valida el token de sesion (JWT firmado con HMAC-SHA256).
 *
 * La clave de firma viene de la variable de entorno JWT_SECRET (minimo 32 caracteres).
 * Si no esta definida (desarrollo local), se genera una clave aleatoria al arrancar:
 * funciona, pero los tokens dejan de servir cada vez que se reinicia la API.
 */
@Service
public class TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final SeguridadProperties seguridad;
    private final Clock reloj;

    public TokenService(@Value("${JWT_SECRET:}") String secreto, SeguridadProperties seguridad, Clock reloj) {
        SecretKey clave = crearClave(secreto);
        this.encoder = NimbusJwtEncoder.withSecretKey(clave).algorithm(MacAlgorithm.HS256).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        this.seguridad = seguridad;
        this.reloj = reloj;
    }

    /** Token valido por los minutos de inactividad configurados (30 por defecto). */
    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = reloj.instant();
        Instant expira = ahora.plus(seguridad.minutosInactividadSesion(), ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("talentmatch")
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_NOMBRE, usuario.getNombre())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
        return new TokenEmitido(token, expira);
    }

    public JwtDecoder decoder() {
        return decoder;
    }

    private static SecretKey crearClave(String secreto) {
        byte[] bytes;
        if (secreto == null || secreto.isBlank()) {
            LOG.warn("JWT_SECRET no esta definido: se usa una clave aleatoria (solo para desarrollo).");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = secreto.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /** Token y momento en que vence. */
    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
