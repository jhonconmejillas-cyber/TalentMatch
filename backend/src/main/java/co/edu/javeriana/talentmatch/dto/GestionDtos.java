package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.model.Notificacion;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Contratos JSON de asignaciones y notificaciones. */
public final class GestionDtos {

    private GestionDtos() {
    }

    public record AsignacionDto(Long id, Long empleadoId, String empleado, Long proyectoId, String proyecto,
                                String cargo, LocalDate fechaInicio, LocalDate fechaFin, MotivoFinAsignacion motivoFin) {

        public static AsignacionDto de(AsignacionProyecto a) {
            return new AsignacionDto(a.getId(), a.getEmpleado().getId(), a.getEmpleado().getNombre(),
                    a.getProyecto().getId(), a.getProyecto().getNombre(), a.getCargo(), a.getFechaInicio(),
                    a.getFechaFin(), a.getMotivoFin());
        }
    }

    public record LiberarAsignacionRequest(
            @NotNull(message = "Indica el motivo: RENUNCIA o CIERRE_PROYECTO.") MotivoFinAsignacion motivo) {
    }

    public record NotificacionDto(Long id, TipoNotificacion tipo, String titulo, String mensaje,
                                  LocalDateTime fecha, boolean leida, Long vacanteId, Long postulacionId) {

        public static NotificacionDto de(Notificacion n) {
            return new NotificacionDto(n.getId(), n.getTipo(), n.getTitulo(), n.getMensaje(), n.getFecha(),
                    n.isLeida(), n.getVacante() == null ? null : n.getVacante().getId(),
                    n.getPostulacion() == null ? null : n.getPostulacion().getId());
        }
    }
}
