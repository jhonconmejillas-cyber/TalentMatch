package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.HabilidadDtos.AgregarHabilidadRequest;
import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadCatalogoDto;
import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadEmpleadoDto;
import co.edu.javeriana.talentmatch.dto.HabilidadDtos.RevisarHabilidadRequest;
import co.edu.javeriana.talentmatch.model.EstadoHabilidad;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.HabilidadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Catalogo, habilidades propias y aval del manager (RF-06, RF-07). */
@RestController
@RequestMapping("/api/v1")
public class HabilidadController {

    private final HabilidadService habilidades;

    public HabilidadController(HabilidadService habilidades) {
        this.habilidades = habilidades;
    }

    @GetMapping("/habilidades")
    public List<HabilidadCatalogoDto> catalogo() {
        return habilidades.catalogo();
    }

    @GetMapping("/empleados/me/habilidades")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public List<HabilidadEmpleadoDto> mias(@AuthenticationPrincipal Jwt token) {
        return habilidades.misHabilidades(Sesion.idUsuario(token));
    }

    @PostMapping("/empleados/me/habilidades")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public HabilidadEmpleadoDto agregar(@AuthenticationPrincipal Jwt token,
                                        @Valid @RequestBody AgregarHabilidadRequest datos) {
        return habilidades.agregar(Sesion.idUsuario(token), datos);
    }

    /** Bandeja del manager. Sin "estado" devuelve todas las de su equipo. */
    @GetMapping("/equipo/habilidades")
    @PreAuthorize("hasRole('MANAGER')")
    public List<HabilidadEmpleadoDto> deMiEquipo(@AuthenticationPrincipal Jwt token,
                                                 @RequestParam(required = false) EstadoHabilidad estado) {
        return habilidades.deMiEquipo(Sesion.idUsuario(token), estado);
    }

    @PatchMapping("/habilidades-empleado/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public HabilidadEmpleadoDto revisar(@AuthenticationPrincipal Jwt token, @PathVariable Long id,
                                        @Valid @RequestBody RevisarHabilidadRequest datos) {
        return habilidades.revisar(Sesion.idUsuario(token), id, datos.estado());
    }
}
