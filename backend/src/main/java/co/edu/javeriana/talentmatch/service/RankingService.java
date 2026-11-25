package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.config.CostoRotacionProperties;
import co.edu.javeriana.talentmatch.config.RankingProperties;
import co.edu.javeriana.talentmatch.dto.MatchingDtos.DesgloseDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.CandidatoDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.CostoRotacionDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.DesgloseCandidatoDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.Indicador;
import co.edu.javeriana.talentmatch.dto.RankingDtos.PostuladoBajoUmbralDto;
import co.edu.javeriana.talentmatch.dto.RankingDtos.RankingDto;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.Rol;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import co.edu.javeriana.talentmatch.service.MatchingService.Calculo;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ranking de candidatos de una vacante (CU_14, HU-001):
 * - RN-27: solo candidatos con Puntaje Final >= umbral.
 * - RN-14: orden por Puntaje Final descendente, calculado siempre en el servidor y sin
 *   ninguna operacion que permita reordenarlo.
 * - RN-12: las peticiones se resuelven por lotes cada 3 dias desde la activacion.
 */
@Service
public class RankingService {

    private final VacanteRepository vacantes;
    private final PostulacionRepository postulaciones;
    private final UsuarioRepository usuarios;
    private final MatchingService matching;
    private final ParametrosService parametros;
    private final RankingProperties ranking;
    private final CostoRotacionProperties costos;
    private final Clock reloj;

    public RankingService(VacanteRepository vacantes, PostulacionRepository postulaciones, UsuarioRepository usuarios,
                          MatchingService matching, ParametrosService parametros, RankingProperties ranking,
                          CostoRotacionProperties costos, Clock reloj) {
        this.vacantes = vacantes;
        this.postulaciones = postulaciones;
        this.usuarios = usuarios;
        this.matching = matching;
        this.parametros = parametros;
        this.ranking = ranking;
        this.costos = costos;
        this.reloj = reloj;
    }

    /** Alias estable y no identificable del candidato. */
    public static String alias(Empleado e) {
        return String.format("Empleado_%03d", e.getId());
    }

    @Transactional(readOnly = true)
    public RankingDto ranking(Long idUsuario, Long idVacante) {
        Vacante vacante = vacanteVisible(idUsuario, idVacante);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime corte = corteCiclo(vacante, ahora);
        double umbral = parametros.umbralViabilidad();

        List<Postulacion> todas = postulaciones.findByVacanteId(idVacante);
        // Vacante activa: candidatos en evaluacion. Vacante cerrada: el ranking final con su resultado.
        List<Postulacion> orden = vacante.estaActiva()
                ? Vacante.calcularRanking(todas, umbral)
                : ordenCompleto(todas).stream().filter(p -> p.esViable(umbral)).toList();
        List<CandidatoDto> candidatos = new ArrayList<>();
        for (int i = 0; i < orden.size(); i++) {
            Postulacion p = orden.get(i);
            candidatos.add(new CandidatoDto(i + 1, p.getId(), alias(p.getEmpleado()), p.getPuntajeFinal(),
                    indicador(p.getPuntajeFinal()), p.getFechaCreacion(),
                    p.estaActiva() && corte != null && p.getFechaCreacion().isBefore(corte), p.getEstado()));
        }
        LocalDateTime proximo = vacante.getFechaActivacion() == null ? null
                : (corte == null ? vacante.getFechaActivacion() : corte).plusDays(parametros.cicloEvaluacionDias());
        return new RankingDto(VacanteDto.de(vacante), umbral, parametros.cicloEvaluacionDias(), corte, proximo,
                candidatos, bajoUmbral(todas, umbral));
    }

    /**
     * RN-28: los postulados con puntaje bajo no entran al ranking, pero el manager debe saber
     * que se postularon. Se excluyen los cancelados por rotacion (RN-09: dejan de aparecer).
     */
    private static List<PostuladoBajoUmbralDto> bajoUmbral(List<Postulacion> todas, double umbral) {
        return todas.stream()
                .filter(p -> !p.esViable(umbral))
                .filter(p -> p.getEstado() != EstadoPostulacion.CANCELADA_POR_ROTACION)
                .sorted(Comparator.comparingDouble(Postulacion::getPuntajeFinal).reversed()
                        .thenComparing(Postulacion::getFechaCreacion))
                .map(p -> new PostuladoBajoUmbralDto(p.getId(), alias(p.getEmpleado()), p.getPuntajeFinal(),
                        p.getFechaCreacion(), p.getEstado()))
                .toList();
    }

    /** Desglose del Match Score y Costo de Rotacion de un candidato (queda en la bitacora). */
    @Transactional
    public DesgloseCandidatoDto desglose(Long idUsuario, Long idPostulacion) {
        Postulacion p = postulaciones.findById(idPostulacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la postulación " + idPostulacion + "."));
        vacanteVisible(idUsuario, p.getVacante().getId());
        Calculo calculo = matching.calcular(p.getEmpleado(), p.getVacante());
        return new DesgloseCandidatoDto(p.getId(), alias(p.getEmpleado()), p.getPuntajeFinal(),
                DesgloseDto.de(calculo.resultado()), costoRotacion());
    }

    /** RN-26: costo de perder a la persona frente al de moverla. No altera el ranking (PEN-02). */
    public CostoRotacionDto costoRotacion() {
        if (!Boolean.TRUE.equals(costos.habilitado())) {
            return null;
        }
        int salida = costos.salida().total();
        int interna = costos.rotacionInterna().total();
        return new CostoRotacionDto(salida, interna, salida - interna, String.format(
                "Si aceptas a este candidato ahorras aproximadamente USD %,d frente a reemplazarlo si renunciara.",
                salida - interna));
    }

    /**
     * RN-12: corte del ultimo ciclo completo desde la activacion. Solo las peticiones anteriores
     * a ese corte forman el lote que el manager puede resolver. Null si aun no se cumple el primer ciclo.
     */
    public LocalDateTime corteCiclo(Vacante vacante, LocalDateTime ahora) {
        if (vacante.getFechaActivacion() == null) {
            return null;
        }
        // Se cuentan dias completos en la zona horaria del sistema (evita errores con cambios de hora)
        long dias = ChronoUnit.DAYS.between(vacante.getFechaActivacion().atZone(reloj.getZone()), ahora.atZone(reloj.getZone()));
        long ciclos = dias / parametros.cicloEvaluacionDias();
        return ciclos == 0 ? null : vacante.getFechaActivacion().plusDays(ciclos * parametros.cicloEvaluacionDias());
    }

    private Indicador indicador(double puntaje) {
        if (puntaje >= ranking.indicadorVerde()) {
            return Indicador.VERDE;
        }
        return puntaje >= ranking.indicadorAmarillo() ? Indicador.AMARILLO : Indicador.ROJO;
    }

    /** El ranking lo ven el manager que publico la vacante y RR.HH. */
    Vacante vacanteVisible(Long idUsuario, Long idVacante) {
        Vacante vacante = vacantes.findById(idVacante)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la vacante " + idVacante + "."));
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("Sesión no válida."));
        if (usuario.getRol() != Rol.RRHH && !usuario.mismoUsuarioQue(vacante.getManager())) {
            throw new AccesoDenegadoException("Solo el manager que publicó la vacante puede ver su ranking.");
        }
        return vacante;
    }

    /** Todas las postulaciones de la vacante ordenadas por Puntaje Final (para registrar posiciones). */
    List<Postulacion> ordenCompleto(Long idVacante) {
        return ordenCompleto(postulaciones.findByVacanteId(idVacante));
    }

    private static List<Postulacion> ordenCompleto(List<Postulacion> todas) {
        return todas.stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.ACTIVA || p.getEstado() == EstadoPostulacion.ACEPTADA
                        || (p.getEstado() == EstadoPostulacion.RECHAZADA && p.getPosicionRanking() != null))
                .sorted(Comparator.comparingDouble(Postulacion::getPuntajeFinal).reversed()
                        .thenComparing(Postulacion::getFechaCreacion))
                .toList();
    }
}
