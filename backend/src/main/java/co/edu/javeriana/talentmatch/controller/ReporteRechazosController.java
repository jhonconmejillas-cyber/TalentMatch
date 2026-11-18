package co.edu.javeriana.talentmatch.controller;

import co.edu.javeriana.talentmatch.model.ReporteMensual;
import co.edu.javeriana.talentmatch.service.DatosInvalidosException;
import co.edu.javeriana.talentmatch.service.ReporteRechazosService;
import co.edu.javeriana.talentmatch.service.ReporteRechazosService.Reporte;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reporte mensual de patrones de rechazo para RR.HH. (HU-002). */
@RestController
@RequestMapping("/api/v1/reportes/rechazos")
@PreAuthorize("hasRole('RRHH')")
public class ReporteRechazosController {

    private final ReporteRechazosService reportes;
    private final Clock reloj;

    public ReporteRechazosController(ReporteRechazosService reportes, Clock reloj) {
        this.reportes = reportes;
        this.reloj = reloj;
    }

    /** Sin periodo: el mes anterior (el mismo que genera el proceso nocturno). */
    @GetMapping
    public Reporte reporte(@RequestParam(required = false) String periodo) {
        return reportes.calcular(periodo(periodo));
    }

    @GetMapping("/archivo")
    public ResponseEntity<byte[]> archivo(@RequestParam(required = false) String periodo) {
        YearMonth mes = periodo(periodo);
        byte[] csv = reportes.csv(reportes.calcular(mes)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-rechazos-" + mes + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/generados")
    public List<ReporteMensual> generados() {
        return reportes.generados();
    }

    private YearMonth periodo(String texto) {
        if (texto == null || texto.isBlank()) {
            return YearMonth.now(reloj).minusMonths(1);
        }
        try {
            return YearMonth.parse(texto);
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("periodo", "El periodo debe tener el formato AAAA-MM (ejemplo: 2026-10).");
        }
    }
}
