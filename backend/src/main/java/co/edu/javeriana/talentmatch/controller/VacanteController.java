package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.VacanteDtos.FiltroVacantes;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.PublicarVacanteRequest;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.VacanteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Vacantes internas (RF-11, RF-13). */
@RestController
@RequestMapping("/api/v1")
public class VacanteController {

    private final VacanteService vacantes;

    public VacanteController(VacanteService vacantes) {
        this.vacantes = vacantes;
    }

    @PostMapping("/vacantes")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public VacanteDto publicar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody PublicarVacanteRequest datos) {
        return vacantes.publicar(Sesion.idUsuario(token), datos);
    }

    @GetMapping("/vacantes")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER', 'RRHH')")
    public List<VacanteDto> buscar(@RequestParam(required = false) String titulo,
                                   @RequestParam(required = false) Long habilidadId,
                                   @RequestParam(required = false) String area,
                                   @RequestParam(required = false) Senioridad senioridad) {
        return vacantes.buscar(new FiltroVacantes(titulo, habilidadId, area, senioridad));
    }

    @GetMapping("/vacantes/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER', 'RRHH')")
    public VacanteDto detalle(@PathVariable Long id) {
        return vacantes.detalle(id);
    }

    /** Vacantes publicadas por el manager autenticado (todas sus estados). */
    @GetMapping("/equipo/vacantes")
    @PreAuthorize("hasRole('MANAGER')")
    public List<VacanteDto> mias(@AuthenticationPrincipal Jwt token) {
        return vacantes.deManager(Sesion.idUsuario(token));
    }
}
