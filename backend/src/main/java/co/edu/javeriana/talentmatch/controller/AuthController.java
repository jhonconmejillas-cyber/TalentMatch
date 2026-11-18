package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.AuthDtos.LoginRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.LoginResponse;
import co.edu.javeriana.talentmatch.dto.AuthDtos.RegistroRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.UsuarioDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.AuthService;
import co.edu.javeriana.talentmatch.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Registro, login y datos de la sesion (RF-01, RF-02). */
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registrar(@Valid @RequestBody RegistroRequest datos) {
        return auth.registrar(datos);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return auth.iniciarSesion(datos);
    }

    /** Usuario y rol de quien hace la peticion. */
    @GetMapping("/me")
    public UsuarioDto yo(@AuthenticationPrincipal Jwt token) {
        return usuarios.buscar(Sesion.idUsuario(token));
    }
}
