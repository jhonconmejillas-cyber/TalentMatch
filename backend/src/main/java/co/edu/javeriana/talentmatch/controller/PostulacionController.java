package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.PostulacionDtos.ElegibilidadDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.MiPostulacionDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.PostulacionCreadaDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.PostulacionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Postulaciones del empleado autenticado (RF-19, RF-20, RF-31). */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
public class PostulacionController {

    private final PostulacionService postulaciones;

    public PostulacionController(PostulacionService postulaciones) {
        this.postulaciones = postulaciones;
    }

    @GetMapping("/vacantes/{id}/elegibilidad")
    public ElegibilidadDto elegibilidad(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return postulaciones.elegibilidad(Sesion.idUsuario(token), id);
    }

    @PostMapping("/vacantes/{id}/postulaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PostulacionCreadaDto postularse(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return postulaciones.postularse(Sesion.idUsuario(token), id);
    }

    @GetMapping("/empleados/me/postulaciones")
    public List<MiPostulacionDto> mias(@AuthenticationPrincipal Jwt token) {
        return postulaciones.misPostulaciones(Sesion.idUsuario(token));
    }
}
