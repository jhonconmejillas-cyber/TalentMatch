package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.GestionDtos.AsignacionDto;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.repository.AsignacionProyectoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RR.HH. registra que un empleado deja su asignacion (renuncia o cierre de proyecto), CU_18. */
@Service
public class AsignacionService {

    private final AsignacionProyectoRepository asignaciones;
    private final VacanteAutomaticaService vacantesAutomaticas;
    private final Clock reloj;

    public AsignacionService(AsignacionProyectoRepository asignaciones, VacanteAutomaticaService vacantesAutomaticas,
                             Clock reloj) {
        this.asignaciones = asignaciones;
        this.vacantesAutomaticas = vacantesAutomaticas;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<AsignacionDto> vigentes() {
        return asignaciones.findByFechaFinIsNullOrderByProyectoNombreAsc().stream().map(AsignacionDto::de).toList();
    }

    /**
     * La rotacion aceptada NO se registra aqui: la genera el sistema al aceptar un candidato (RN-15).
     */
    @Transactional
    public VacanteDto liberar(Long idAsignacion, MotivoFinAsignacion motivo) {
        if (motivo == MotivoFinAsignacion.ROTACION_ACEPTADA) {
            throw new DatosInvalidosException("motivo",
                    "La rotación aceptada se registra sola al aceptar un candidato. Usa RENUNCIA o CIERRE_PROYECTO.");
        }
        AsignacionProyecto asignacion = asignaciones.findById(idAsignacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la asignación " + idAsignacion + "."));
        return VacanteDto.de(vacantesAutomaticas.liberarAsignacion(asignacion, motivo, LocalDateTime.now(reloj)));
    }
}
