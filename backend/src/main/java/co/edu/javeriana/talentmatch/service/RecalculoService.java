package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-005 (subtarea 2): el Puntaje Final de cada postulacion se guarda al postularse y el ranking
 * lo lee ya calculado. Solo se recalcula cuando cambia algo que entra al calculo: las habilidades
 * avaladas o la senioridad del empleado. Cada recalculo queda en la bitacora (RNF-06).
 */
@Service
public class RecalculoService {

    private final PostulacionRepository postulaciones;
    private final MatchingService matching;

    public RecalculoService(PostulacionRepository postulaciones, MatchingService matching) {
        this.postulaciones = postulaciones;
        this.matching = matching;
    }

    /** Recalcula las postulaciones ACTIVAS del empleado. Devuelve cuantas actualizo. */
    @Transactional
    public int recalcularPostulacionesActivas(Empleado empleado) {
        List<Postulacion> activas = postulaciones.findByEmpleadoIdAndEstado(empleado.getId(), EstadoPostulacion.ACTIVA);
        for (Postulacion p : activas) {
            p.registrarPuntaje(matching.calcular(empleado, p.getVacante()).puntajeFinal());
        }
        postulaciones.saveAll(activas);
        return activas.size();
    }
}
