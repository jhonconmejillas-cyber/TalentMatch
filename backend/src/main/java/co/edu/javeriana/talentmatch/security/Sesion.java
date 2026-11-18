package co.edu.javeriana.talentmatch.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Datos del usuario autenticado que vienen dentro del token. */
public final class Sesion {

    private Sesion() {
    }

    /** Id del usuario dueno del token (claim "sub"). */
    public static Long idUsuario(Jwt token) {
        return Long.valueOf(token.getSubject());
    }

    public static String rol(Jwt token) {
        return token.getClaimAsString(TokenService.CLAIM_ROL);
    }
}
