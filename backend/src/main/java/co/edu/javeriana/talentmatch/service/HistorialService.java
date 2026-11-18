package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.GestionDtos.AsignacionDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.MiPostulacionDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.model.Rol;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.repository.AsignacionProyectoRepository;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historial de proyectos y rotaciones de un empleado (RF-30, CU_20) con autorizacion por
 * RELACION entre el solicitante y el recurso (HU-006, escenario 1):
 * - RR.HH.: historial completo, incluidas sus postulaciones (RNF-10).
 * - Manager: solo si es su manager actual o el empleado se postulo a una de sus vacantes; y nunca
 *   ve las postulaciones ni los rechazos (solo el empleado y RR.HH. pueden verlos, RNF-10).
 * - Cualquier otro caso se deniega y queda en la bitacora de accesos.
 */
@Service
public class HistorialService {

    private final UsuarioRepository usuarios;
    private final EmpleadoRepository empleados;
    private final AsignacionProyectoRepository asignaciones;
    private final PostulacionRepository postulaciones;

    public HistorialService(UsuarioRepository usuarios, EmpleadoRepository empleados,
                            AsignacionProyectoRepository asignaciones, PostulacionRepository postulaciones) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.asignaciones = asignaciones;
        this.postulaciones = postulaciones;
    }

    public record HistorialDto(Long empleadoId, String empleado, int rotaciones, List<AsignacionDto> proyectos,
                               /** Null para managers: el historial de postulaciones es privado (RNF-10). */
                               List<MiPostulacionDto> postulaciones) {
    }

    @Transactional(readOnly = true)
    public HistorialDto historial(Long idSolicitante, Long idEmpleado) {
        Usuario solicitante = usuarios.findById(idSolicitante)
                .orElseThrow(() -> new AccesoDenegadoException("Sesión no válida."));
        Empleado empleado = empleados.findById(idEmpleado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado " + idEmpleado + "."));

        boolean esRrhh = solicitante.getRol() == Rol.RRHH;
        if (!esRrhh && !(solicitante.getRol() == Rol.MANAGER && tieneRelacion(solicitante, empleado))) {
            throw new AccesoDenegadoException(
                    "No tienes relación con este empleado: no puedes consultar su historial.");
        }
        List<AsignacionDto> proyectos = asignaciones.findByEmpleadoIdOrderByFechaInicioDesc(idEmpleado).stream()
                .map(AsignacionDto::de).toList();
        int rotaciones = (int) proyectos.stream()
                .filter(a -> a.motivoFin() == MotivoFinAsignacion.ROTACION_ACEPTADA).count();
        List<MiPostulacionDto> suyas = esRrhh
                ? postulaciones.findByEmpleadoIdOrderByFechaCreacionDesc(idEmpleado).stream().map(MiPostulacionDto::de).toList()
                : null;
        return new HistorialDto(empleado.getId(), empleado.getNombre(), rotaciones, proyectos, suyas);
    }

    private boolean tieneRelacion(Usuario manager, Empleado empleado) {
        boolean esSuManager = empleado.getManagerActual() != null && empleado.getManagerActual().mismoUsuarioQue(manager);
        return esSuManager || postulaciones.existsByEmpleadoIdAndVacanteManagerId(empleado.getId(), manager.getId());
    }
}
