package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.dto.MatchingDtos.DesgloseDto;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos JSON del ranking y la decision del manager (RF-18, RF-21 ... RF-23). */
public final class RankingDtos {

    private RankingDtos() {
    }

    public enum Indicador { VERDE, AMARILLO, ROJO }

    /**
     * Fila del ranking. El candidato se muestra con un alias (Empleado_007): la decision es
     * por merito y el manager no necesita el nombre para decidir (RN-13, RNF-09).
     */
    public record CandidatoDto(
            int posicion,
            Long postulacionId,
            String candidato,
            double puntajeFinal,
            Indicador indicador,
            LocalDateTime fechaPostulacion,
            /** RN-12: la peticion entra en el lote del ciclo ya cerrado y se puede resolver. */
            boolean habilitadoParaDecidir,
            EstadoPostulacion estado) {
    }

    public record RankingDto(
            VacanteDto vacante,
            double umbralViabilidad,
            int cicloEvaluacionDias,
            /** Corte del ultimo ciclo: las postulaciones anteriores a esta fecha se pueden resolver. */
            LocalDateTime corteCicloActual,
            LocalDateTime proximoCorte,
            List<CandidatoDto> candidatos) {
    }

    /** RN-26: argumento economico aproximado, solo informativo. */
    public record CostoRotacionDto(int costoSiRenuncia, int costoSiRota, int ahorro, String mensaje) {
    }

    /** Desglose de un candidato sin salir del ranking (HU-001, escenario 2). */
    public record DesgloseCandidatoDto(
            Long postulacionId,
            String candidato,
            double puntajeFinal,
            DesgloseDto desglose,
            CostoRotacionDto costoRotacion) {
    }

    public record DecisionDto(Long postulacionId, EstadoPostulacion estado, String mensaje) {
    }
}
