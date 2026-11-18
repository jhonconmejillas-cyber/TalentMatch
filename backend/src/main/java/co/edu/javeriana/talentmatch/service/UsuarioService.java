package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.AuthDtos.ActualizarUsuarioRequest;
import co.edu.javeriana.talentmatch.dto.AuthDtos.UsuarioDto;
import co.edu.javeriana.talentmatch.model.Rol;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de la cuenta propia y administracion de cuentas (RF-02, RF-03). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public UsuarioDto buscar(Long id) {
        return UsuarioDto.de(obtener(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll(Sort.by("nombre")).stream().map(UsuarioDto::de).toList();
    }

    /**
     * RF-03: cambia el rol y/o el estado de una cuenta. El administrador no puede
     * desactivarse ni quitarse el rol a si mismo (quedaria el sistema sin administrador).
     */
    @Transactional
    public UsuarioDto actualizar(Long id, ActualizarUsuarioRequest cambios, Long idAdministrador) {
        Usuario usuario = obtener(id);
        if (id.equals(idAdministrador)
                && (Boolean.FALSE.equals(cambios.activo()) || (cambios.rol() != null && cambios.rol() != Rol.ADMIN))) {
            throw new DatosInvalidosException("rol", "No puedes desactivar ni quitarte el rol de administrador a ti mismo.");
        }
        if (cambios.activo() != null) {
            if (cambios.activo()) {
                usuario.activar();
            } else {
                usuario.desactivar();
            }
            usuarios.saveAndFlush(usuario);
        }
        if (cambios.rol() != null && cambios.rol() != usuario.getRol()) {
            // La clase Java de un usuario depende de su rol, asi que el cambio se hace en la tabla.
            if (cambios.rol() == Rol.EMPLEADO || cambios.rol() == Rol.MANAGER) {
                usuarios.asegurarDatosDeEmpleado(id);
            }
            usuarios.cambiarRol(id, cambios.rol().name());
        }
        return UsuarioDto.de(obtener(id));
    }

    private Usuario obtener(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id + "."));
    }
}
