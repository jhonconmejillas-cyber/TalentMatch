package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.RankingDtos.DecisionDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.DesgloseCandidatoDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.RankingDto;
import co.edu.javeriana.talentmatch.model.ReglaNegocioException;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.DecisionService;
import co.edu.javeriana.talentmatch.service.RankingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Ranking inmutable y decision del manager (RF-18, RF-21 ... RF-23). */
@RestController
@RequestMapping("/api/v1")
public class RankingController {

    private final RankingService ranking;
    private final DecisionService decisiones;

    public RankingController(RankingService ranking, DecisionService decisiones) {
        this.ranking = ranking;
        this.decisiones = decisiones;
    }

    @GetMapping("/vacantes/{id}/ranking")
    @PreAuthorize("hasAnyRole('MANAGER', 'RRHH')")
    public RankingDto ranking(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return ranking.ranking(Sesion.idUsuario(token), id);
    }

    /**
     * RN-14: el ranking es inmutable. Cualquier intento de modificarlo (reordenar, fijar,
     * mover un candidato) se rechaza siempre, sin importar el rol.
     */
    @RequestMapping(value = {"/vacantes/{id}/ranking", "/vacantes/{id}/ranking/**"},
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public void reordenar(@PathVariable Long id) {
        throw new ReglaNegocioException("El ranking es inmutable: nadie puede reordenarlo (RN-14).");
    }

    @GetMapping("/postulaciones/{id}/desglose")
    @PreAuthorize("hasAnyRole('MANAGER', 'RRHH')")
    public DesgloseCandidatoDto desglose(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return ranking.desglose(Sesion.idUsuario(token), id);
    }

    @PostMapping("/postulaciones/{id}/aceptar")
    @PreAuthorize("hasRole('MANAGER')")
    public DecisionDto aceptar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return decisiones.aceptar(Sesion.idUsuario(token), id);
    }

    @PostMapping("/postulaciones/{id}/rechazar")
    @PreAuthorize("hasRole('MANAGER')")
    public DecisionDto rechazar(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return decisiones.rechazar(Sesion.idUsuario(token), id);
    }
}
