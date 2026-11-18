package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.model.Proyecto;
import co.edu.javeriana.talentmatch.model.Senioridad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Contratos JSON del perfil profesional (RF-04, CU_03). */
public final class PerfilDtos {

    private PerfilDtos() {
    }

    public record ProyectoDto(Long id, String nombre, String area) {

        public static ProyectoDto de(Proyecto p) {
            return p == null ? null : new ProyectoDto(p.getId(), p.getNombre(), p.getArea());
        }
    }

    public record PerfilDto(
            Long id,
            String nombre,
            String correo,
            Senioridad senioridad,
            int aniosExperiencia,
            String area,
            LocalDate fechaInicioRol,
            LocalDate fechaIngresoEmpresa,
            ProyectoDto proyectoActual,
            String cargo,
            String manager) {
    }

    /**
     * Datos editables. Las fechas y el proyecto actual solo se pueden declarar
     * mientras esten vacios: despues cambian unicamente con una rotacion aceptada (RN-15).
     */
    public record ActualizarPerfilRequest(
            @NotNull(message = "Selecciona tu senioridad.")
            Senioridad senioridad,
            @NotNull(message = "Escribe tus años de experiencia.")
            @Min(value = 0, message = "Los años de experiencia no pueden ser negativos.")
            @Max(value = 60, message = "Los años de experiencia no pueden superar 60.")
            Integer aniosExperiencia,
            @NotBlank(message = "Escribe tu área (por ejemplo: Backend, Cloud, Datos).")
            @Size(max = 80, message = "El área no puede superar 80 caracteres.")
            String area,
            LocalDate fechaInicioRol,
            LocalDate fechaIngresoEmpresa,
            Long proyectoActualId,
            @Size(max = 120, message = "El cargo no puede superar 120 caracteres.")
            String cargo) {
    }
}
