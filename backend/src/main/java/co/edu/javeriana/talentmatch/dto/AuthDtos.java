package co.edu.javeriana.talentmatch.dto;

import co.edu.javeriana.talentmatch.model.Rol;
import co.edu.javeriana.talentmatch.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Contratos JSON de registro, login y administracion de usuarios (RF-01 ... RF-03). */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank(message = "Escribe tu nombre completo.")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
            String nombre,
            @NotBlank(message = "Escribe tu correo corporativo.")
            @Email(message = "El correo no tiene un formato válido (ejemplo: nombre@empresa.com).")
            String correo,
            @NotBlank(message = "Escribe una contraseña.")
            String contrasena) {
    }

    public record LoginRequest(
            @NotBlank(message = "Escribe tu correo corporativo.") String correo,
            @NotBlank(message = "Escribe tu contraseña.") String contrasena) {
    }

    /** Datos publicos de la cuenta; nunca incluye el hash de la contrasena. */
    public record UsuarioDto(Long id, String nombre, String correo, Rol rol, boolean activo) {

        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.isActivo());
        }
    }

    public record LoginResponse(String token, Instant expiraEn, UsuarioDto usuario) {
    }

    /** RF-03: el administrador cambia el rol y/o activa o desactiva una cuenta. */
    public record ActualizarUsuarioRequest(Rol rol, Boolean activo) {
    }
}
