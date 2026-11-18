package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.model.EstadoHabilidad;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Contratos JSON de habilidades (RF-06, RF-07). */
public final class HabilidadDtos {

    private HabilidadDtos() {
    }

    public record HabilidadCatalogoDto(Long id, String nombre) {

        public static HabilidadCatalogoDto de(Habilidad h) {
            return new HabilidadCatalogoDto(h.getId(), h.getNombre());
        }
    }

    public record HabilidadEmpleadoDto(
            Long id,
            Long habilidadId,
            String habilidad,
            EstadoHabilidad estado,
            String nivelDeclarado,
            LocalDate fechaAgregado,
            LocalDateTime fechaAval,
            Long empleadoId,
            String empleado) {

        public static HabilidadEmpleadoDto de(HabilidadEmpleado he) {
            return new HabilidadEmpleadoDto(he.getId(), he.getHabilidad().getId(), he.getHabilidad().getNombre(),
                    he.getEstado(), he.getNivelDeclarado(), he.getFechaAgregado(), he.getFechaAval(),
                    he.getEmpleado().getId(), he.getEmpleado().getNombre());
        }
    }

    public record AgregarHabilidadRequest(
            @NotNull(message = "Selecciona una habilidad del catálogo.") Long habilidadId,
            @Size(max = 40, message = "El nivel no puede superar 40 caracteres.") String nivelDeclarado) {
    }

    /** El manager solo puede pasar la habilidad a AVALADO o RECHAZADO. */
    public record RevisarHabilidadRequest(
            @NotNull(message = "Indica si la habilidad queda avalada o rechazada.") EstadoHabilidad estado) {
    }
}
