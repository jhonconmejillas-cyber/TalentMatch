package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadCatalogoDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.HabilidadRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista de deseos: habilidades que el empleado quiere aprender (RF-09, CU_07). */
@Service
public class WishlistService {

    private final EmpleadoRepository empleados;
    private final HabilidadRepository habilidades;

    public WishlistService(EmpleadoRepository empleados, HabilidadRepository habilidades) {
        this.empleados = empleados;
        this.habilidades = habilidades;
    }

    @Transactional(readOnly = true)
    public List<HabilidadCatalogoDto> de(Long idEmpleado) {
        return ordenada(empleado(idEmpleado));
    }

    @Transactional
    public List<HabilidadCatalogoDto> agregar(Long idEmpleado, Long idHabilidad) {
        Empleado e = empleado(idEmpleado);
        e.agregarAWishlist(habilidad(idHabilidad));
        return ordenada(e);
    }

    @Transactional
    public List<HabilidadCatalogoDto> quitar(Long idEmpleado, Long idHabilidad) {
        Empleado e = empleado(idEmpleado);
        e.quitarDeWishlist(habilidad(idHabilidad));
        return ordenada(e);
    }

    private static List<HabilidadCatalogoDto> ordenada(Empleado e) {
        return e.getWishlist().stream().sorted(Comparator.comparing(Habilidad::getNombre))
                .map(HabilidadCatalogoDto::de).toList();
    }

    private Empleado empleado(Long id) {
        return empleados.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado " + id + "."));
    }

    private Habilidad habilidad(Long id) {
        return habilidades.findById(id)
                .orElseThrow(() -> new DatosInvalidosException("habilidadId", "La habilidad no existe en el catálogo."));
    }
}
