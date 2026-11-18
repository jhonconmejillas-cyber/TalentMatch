package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.config.SeguridadProperties;
import co.edu.javeriana.talentmatch.dto.AuthDtos.LoginRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.LoginResponse;
import co.edu.javeriana.talentmatch.dto.AuthDtos.RegistroRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.UsuarioDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import co.edu.javeriana.talentmatch.security.TokenService;
import co.edu.javeriana.talentmatch.security.TokenService.TokenEmitido;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro e inicio de sesion (CU_01, CU_02). */
@Service
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final SeguridadProperties seguridad;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens,
                       SeguridadProperties seguridad) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.seguridad = seguridad;
    }

    /**
     * RF-01: crea la cuenta con rol EMPLEADO. Valida el dominio corporativo, la longitud
     * minima de la contrasena y que el correo no este registrado. La contrasena se guarda
     * con hash BCrypt, nunca en texto plano (RNF-07).
     */
    @Transactional
    public UsuarioDto registrar(RegistroRequest datos) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        String dominio = correo.substring(correo.indexOf('@') + 1);
        if (!seguridad.dominiosCorreoCorporativo().contains(dominio)) {
            throw new DatosInvalidosException("correo",
                    "Usa tu correo corporativo (@" + String.join(", @", seguridad.dominiosCorreoCorporativo()) + ").");
        }
        if (datos.contrasena().length() < seguridad.longitudMinimaPassword()) {
            throw new DatosInvalidosException("contrasena",
                    "La contraseña debe tener al menos " + seguridad.longitudMinimaPassword() + " caracteres.");
        }
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new DatosInvalidosException("correo", "Ya existe una cuenta con este correo. Inicia sesión.");
        }
        Empleado nuevo = new Empleado(datos.nombre().trim(), correo, passwordEncoder.encode(datos.contrasena()));
        return UsuarioDto.de(usuarios.save(nuevo));
    }

    /** RF-02: autentica y devuelve el token con el rol del usuario. */
    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(datos.correo().trim())
                .orElseThrow(() -> new CredencialesInvalidasException(CREDENCIALES_INVALIDAS));
        if (!passwordEncoder.matches(datos.contrasena(), usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasException(CREDENCIALES_INVALIDAS);
        }
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("Tu cuenta está desactivada. Contacta al administrador.");
        }
        TokenEmitido token = tokens.emitir(usuario);
        return new LoginResponse(token.token(), token.expiraEn(), UsuarioDto.de(usuario));
    }
}
