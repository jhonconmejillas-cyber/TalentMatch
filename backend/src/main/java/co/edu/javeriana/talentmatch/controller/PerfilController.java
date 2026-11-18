package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.PerfilDtos.ActualizarPerfilRequest;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.PerfilDto;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.ProyectoDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.PerfilService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil profesional del empleado autenticado (RF-04). Managers incluidos: tambien son empleados. */
@RestController
@RequestMapping("/api/v1")
public class PerfilController {

    private final PerfilService perfiles;

    public PerfilController(PerfilService perfiles) {
        this.perfiles = perfiles;
    }

    @GetMapping("/empleados/me/perfil")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public PerfilDto consultar(@AuthenticationPrincipal Jwt token) {
        return perfiles.consultar(Sesion.idUsuario(token));
    }

    @PutMapping("/empleados/me/perfil")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public PerfilDto actualizar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody ActualizarPerfilRequest datos) {
        return perfiles.actualizar(Sesion.idUsuario(token), datos);
    }

    /** Catalogo de proyectos activos, para elegir el proyecto actual. */
    @GetMapping("/proyectos")
    public List<ProyectoDto> proyectos() {
        return perfiles.proyectosActivos();
    }
}
