package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadCatalogoDto;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.ProyectoDto;
import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.OrigenVacante;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.model.Vacante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Contratos JSON de vacantes (RF-11, RF-13). */
public final class VacanteDtos {

    private VacanteDtos() {
    }

    public record VacanteDto(
            Long id,
            String titulo,
            String descripcion,
            Senioridad senioridad,
            String area,
            EstadoVacante estado,
            OrigenVacante origen,
            LocalDateTime fechaActivacion,
            ProyectoDto proyecto,
            String manager,
            List<HabilidadCatalogoDto> habilidadesCriticas,
            List<HabilidadCatalogoDto> habilidadesDeseables) {

        public static VacanteDto de(Vacante v) {
            return new VacanteDto(v.getId(), v.getTitulo(), v.getDescripcion(), v.getSenioridad(), v.getArea(),
                    v.getEstado(), v.getOrigen(), v.getFechaActivacion(), ProyectoDto.de(v.getProyecto()),
                    v.getManager() == null ? null : v.getManager().getNombre(),
                    ordenadas(v.getHabilidadesCriticas()), ordenadas(v.getHabilidadesDeseables()));
        }

        private static List<HabilidadCatalogoDto> ordenadas(Set<Habilidad> habilidades) {
            return habilidades.stream().sorted(Comparator.comparing(Habilidad::getNombre))
                    .map(HabilidadCatalogoDto::de).toList();
        }
    }

    public record PublicarVacanteRequest(
            @NotBlank(message = "Escribe el título de la vacante.")
            @Size(max = 120, message = "El título no puede superar 120 caracteres.")
            String titulo,
            @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres.")
            String descripcion,
            @NotNull(message = "Selecciona la senioridad requerida.")
            Senioridad senioridad,
            @NotBlank(message = "Escribe el área de la vacante.")
            @Size(max = 80, message = "El área no puede superar 80 caracteres.")
            String area,
            Long proyectoId,
            @NotEmpty(message = "Selecciona al menos una habilidad crítica.")
            List<Long> habilidadesCriticasIds,
            List<Long> habilidadesDeseablesIds) {
    }

    /** Filtros de busqueda (RF-13). Todos son opcionales. */
    public record FiltroVacantes(String titulo, Long habilidadId, String area, Senioridad senioridad) {
    }
}
