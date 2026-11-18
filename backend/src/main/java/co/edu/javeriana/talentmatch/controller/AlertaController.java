package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.model.Alerta;
import co.edu.javeriana.talentmatch.model.EstadoAlerta;
import co.edu.javeriana.talentmatch.model.TipoAlerta;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.AlertaService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bandeja de alertas de RR.HH. (RF-14, RF-32, CU_24). */
@RestController
@RequestMapping("/api/v1/alertas")
@PreAuthorize("hasRole('RRHH')")
public class AlertaController {

    private final AlertaService alertas;

    public AlertaController(AlertaService alertas) {
        this.alertas = alertas;
    }

    public record AlertaDto(Long id, TipoAlerta tipo, EstadoAlerta estado, LocalDateTime fecha, Long managerId,
                            String manager, Long vacanteId, String vacante, String detalle,
                            LocalDateTime fechaResolucion) {

        static AlertaDto de(Alerta a) {
            return new AlertaDto(a.getId(), a.getTipo(), a.getEstado(), a.getFecha(),
                    a.getManager() == null ? null : a.getManager().getId(),
                    a.getManager() == null ? null : a.getManager().getNombre(),
                    a.getVacante() == null ? null : a.getVacante().getId(),
                    a.getVacante() == null ? null : a.getVacante().getTitulo(),
                    a.getDetalle(), a.getFechaResolucion());
        }
    }

    @GetMapping
    public List<AlertaDto> listar(@RequestParam(required = false) EstadoAlerta estado) {
        return alertas.listar(estado).stream().map(AlertaDto::de).toList();
    }

    @PatchMapping("/{id}")
    public AlertaDto resolver(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return AlertaDto.de(alertas.resolver(Sesion.idUsuario(token), id));
    }
}
