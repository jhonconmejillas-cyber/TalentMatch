package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.GestionDtos.AsignacionDto;
import co.edu.javeriana.talentmatch.dto.GestionDtos.LiberarAsignacionRequest;
import co.edu.javeriana.talentmatch.dto.GestionDtos.NotificacionDto;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.AsignacionService;
import co.edu.javeriana.talentmatch.service.NotificacionService;
import co.edu.javeriana.talentmatch.service.VacanteService;
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

/** Asignaciones y vacantes por estado (RR.HH.) y notificaciones de cada usuario. */
@RestController
@RequestMapping("/api/v1")
public class GestionController {

    private final AsignacionService asignaciones;
    private final VacanteService vacantes;
    private final NotificacionService notificaciones;

    public GestionController(AsignacionService asignaciones, VacanteService vacantes,
                             NotificacionService notificaciones) {
        this.asignaciones = asignaciones;
        this.vacantes = vacantes;
        this.notificaciones = notificaciones;
    }

    @GetMapping("/asignaciones")
    @PreAuthorize("hasRole('RRHH')")
    public List<AsignacionDto> asignacionesVigentes() {
        return asignaciones.vigentes();
    }

    /** CU_18: renuncia o cierre de proyecto -> la vacante se crea y se activa de inmediato. */
    @PostMapping("/asignaciones/{id}/liberar")
    @PreAuthorize("hasRole('RRHH')")
    @ResponseStatus(HttpStatus.CREATED)
    public VacanteDto liberar(@PathVariable Long id, @Valid @RequestBody LiberarAsignacionRequest datos) {
        return asignaciones.liberar(id, datos.motivo());
    }

    /** Panel de vacantes por estado para RR.HH. (HU-009, subtarea 5). */
    @GetMapping("/gestion/vacantes")
    @PreAuthorize("hasRole('RRHH')")
    public List<VacanteDto> vacantesPorEstado(@RequestParam(required = false) EstadoVacante estado) {
        return vacantes.todas(estado);
    }

    @GetMapping("/notificaciones")
    public List<NotificacionDto> misNotificaciones(@AuthenticationPrincipal Jwt token) {
        return notificaciones.de(Sesion.idUsuario(token)).stream().map(NotificacionDto::de).toList();
    }

    @PatchMapping("/notificaciones/{id}")
    public NotificacionDto marcarLeida(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return NotificacionDto.de(notificaciones.marcarLeida(Sesion.idUsuario(token), id));
    }
}
