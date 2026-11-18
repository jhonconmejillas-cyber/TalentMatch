package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.service.ReporteEjecutivoService;
import co.edu.javeriana.talentmatch.service.ReporteEjecutivoService.ReporteTop;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reporte ejecutivo top 10 (RF-29). Solo Lider Ejecutivo y RR.HH. (RNF-07). */
@RestController
@RequestMapping("/api/v1/reportes/top10")
@PreAuthorize("hasAnyRole('LIDER_EJECUTIVO', 'RRHH')")
public class ReporteEjecutivoController {

    private final ReporteEjecutivoService reporte;

    public ReporteEjecutivoController(ReporteEjecutivoService reporte) {
        this.reporte = reporte;
    }

    @GetMapping
    public ReporteTop top() {
        return reporte.top();
    }
}
