package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.AuthDtos.ActualizarUsuarioRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.UsuarioDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.UsuarioService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Gestion de cuentas y roles, solo para el Administrador (RF-03, CU_23). */
@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<UsuarioDto> listar() {
        return usuarios.listar();
    }

    @PatchMapping("/{id}")
    public UsuarioDto actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest cambios,
                                 @AuthenticationPrincipal Jwt token) {
        return usuarios.actualizar(id, cambios, Sesion.idUsuario(token));
    }
}
