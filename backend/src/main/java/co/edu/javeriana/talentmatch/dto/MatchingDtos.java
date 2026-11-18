package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Resultado;
import java.util.List;

/** Contratos JSON del Puntaje Final (RF-15, RF-16, RF-17). */
public final class MatchingDtos {

    private MatchingDtos() {
    }

    /** Desglose explicable del Match Score (sin IA, RN §10). */
    public record DesgloseDto(
            double porcentajeHabilidades,
            double pesoHabilidades,
            double ajusteSenioridad,
            double pesoSenioridad,
            List<String> habilidadesCoincidentes,
            List<String> habilidadesFaltantes,
            List<String> habilidadesSinAvalar) {

        public static DesgloseDto de(Resultado r) {
            return new DesgloseDto(r.porcentajeHabilidades(), r.pesoHabilidades(), r.ajusteSenioridad(),
                    r.pesoSenioridad(), r.coincidentes(), r.faltantes(), r.sinAvalar());
        }
    }

    public record MiPuntajeDto(
            Long vacanteId,
            double matchScore,
            double puntajeFinal,
            double umbralViabilidad,
            boolean viable,
            /** RF-17: aviso de posibilidades bajas; no bloquea la postulacion (RN-28). */
            String aviso,
            DesgloseDto desglose) {
    }
}
