package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadCatalogoDto;
import co.edu.javeriana.talentmatch.dto.ResultadoDtos.ResultadoDto;
import co.edu.javeriana.talentmatch.security.Sesion;
import co.edu.javeriana.talentmatch.service.ResultadoService;
import co.edu.javeriana.talentmatch.service.WishlistService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Resultado anonimo del rechazo (RF-27) y lista de deseos (RF-09). */
@RestController
@RequestMapping("/api/v1")
public class ResultadoController {

    private final ResultadoService resultados;
    private final WishlistService wishlist;

    public ResultadoController(ResultadoService resultados, WishlistService wishlist) {
        this.resultados = resultados;
        this.wishlist = wishlist;
    }

    @GetMapping("/postulaciones/{id}/resultado")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER', 'RRHH')")
    public ResultadoDto resultado(@AuthenticationPrincipal Jwt token, @PathVariable Long id) {
        return resultados.resultado(Sesion.idUsuario(token), id);
    }

    @GetMapping("/empleados/me/wishlist")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public List<HabilidadCatalogoDto> miWishlist(@AuthenticationPrincipal Jwt token) {
        return wishlist.de(Sesion.idUsuario(token));
    }

    @PostMapping("/empleados/me/wishlist/{habilidadId}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public List<HabilidadCatalogoDto> agregar(@AuthenticationPrincipal Jwt token, @PathVariable Long habilidadId) {
        return wishlist.agregar(Sesion.idUsuario(token), habilidadId);
    }

    @DeleteMapping("/empleados/me/wishlist/{habilidadId}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'MANAGER')")
    public List<HabilidadCatalogoDto> quitar(@AuthenticationPrincipal Jwt token, @PathVariable Long habilidadId) {
        return wishlist.quitar(Sesion.idUsuario(token), habilidadId);
    }
}
