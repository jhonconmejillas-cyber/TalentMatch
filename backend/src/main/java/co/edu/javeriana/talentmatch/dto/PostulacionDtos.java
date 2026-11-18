package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.service.ReporteEjecutivoService;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Contratos JSON de postulaciones (RF-19, RF-20, RF-31). */
public final class PostulacionDtos {

    private PostulacionDtos() {
    }

    /**
     * Si el empleado puede postularse y, si no, por que y DESDE CUANDO (HU-008:
     * ningun mensaje de bloqueo deja al usuario sin saber cuando podra postularse).
     */
    public record ElegibilidadDto(boolean puedePostularse, String motivo, LocalDate fechaHabilitacion,
                                  boolean yaPostulado) {
    }

    /** Postulacion vista por el propio empleado. */
    public record MiPostulacionDto(
            Long id,
            Long vacanteId,
            String vacante,
            String area,
            EstadoPostulacion estado,
            double puntajeFinal,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaResolucion,
            Integer posicionRanking,
            Integer totalCandidatos,
            /** RN-22 en la rotacion confirmada; RN-09 en la cancelada. */
            String mensaje) {

        public static MiPostulacionDto de(Postulacion p) {
            String mensaje = switch (p.getEstado()) {
                case ACEPTADA -> ReporteEjecutivoService.MENSAJE_INCENTIVO;
                case CANCELADA_POR_ROTACION -> "Cancelada por rotación aceptada: aceptaste otra vacante.";
                default -> null;
            };
            return new MiPostulacionDto(p.getId(), p.getVacante().getId(), p.getVacante().getTitulo(),
                    p.getVacante().getArea(), p.getEstado(), p.getPuntajeFinal(), p.getFechaCreacion(),
                    p.getFechaResolucion(), p.getPosicionRanking(), p.getTotalCandidatos(), mensaje);
        }
    }

    public record PostulacionCreadaDto(MiPostulacionDto postulacion, boolean viable, String aviso) {
    }
}
