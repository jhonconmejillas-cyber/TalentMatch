package co.edu.javeriana.talentmatch.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resultado anonimo de una postulacion rechazada (RN-18, RF-27).
 *
 * IMPORTANTE: este contrato NO tiene ni puede tener campos que identifiquen a la persona
 * seleccionada (id, nombre, correo, area, foto...). Solo su puntaje y sus habilidades.
 * Una prueba automatica revisa el JSON de respuesta.
 */
public final class ResultadoDtos {

    private ResultadoDtos() {
    }

    public record FilaComparacion(String habilidad, boolean laTengoAvalada, boolean laTieneElSeleccionado,
                                  boolean enMiWishlist, Long habilidadId) {
    }

    public record ResultadoDto(
            Long postulacionId,
            String vacante,
            double miPuntajeFinal,
            /** Null mientras la vacante no se haya cerrado con un seleccionado. */
            Double puntajeSeleccionado,
            Integer posicion,
            Integer totalCandidatos,
            List<FilaComparacion> comparacion,
            /** RN-10: desde cuando podria volver a postularse a ese puesto (si se abre una nueva rotacion). */
            LocalDate repostulacionDesde,
            String queSigue) {
    }
}
