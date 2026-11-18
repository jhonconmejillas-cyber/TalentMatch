package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.HabilidadDtos.AgregarHabilidadRequest;
import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadCatalogoDto;
import co.edu.javeriana.talentmatch.dto.HabilidadDtos.HabilidadEmpleadoDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoHabilidad;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.HabilidadEmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.HabilidadRepository;
import co.edu.javeriana.talentmatch.repository.ManagerRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Habilidades del perfil y su aval (RN-19, RN-20, CU_05, CU_06).
 */
@Service
public class HabilidadService {

    private final HabilidadRepository catalogo;
    private final HabilidadEmpleadoRepository habilidadesEmpleado;
    private final EmpleadoRepository empleados;
    private final ManagerRepository managers;
    private final RecalculoService recalculo;
    private final Clock reloj;

    public HabilidadService(HabilidadRepository catalogo, HabilidadEmpleadoRepository habilidadesEmpleado,
                            EmpleadoRepository empleados, ManagerRepository managers, RecalculoService recalculo,
                            Clock reloj) {
        this.catalogo = catalogo;
        this.habilidadesEmpleado = habilidadesEmpleado;
        this.empleados = empleados;
        this.managers = managers;
        this.recalculo = recalculo;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<HabilidadCatalogoDto> catalogo() {
        return catalogo.findAllByOrderByNombreAsc().stream().map(HabilidadCatalogoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<HabilidadEmpleadoDto> misHabilidades(Long idEmpleado) {
        return habilidadesEmpleado.findByEmpleadoIdOrderByFechaAgregadoDesc(idEmpleado).stream()
                .map(HabilidadEmpleadoDto::de).toList();
    }

    /** RF-06 / RN-19: la habilidad entra al perfil en estado AGREGADO y no cuenta hasta ser avalada. */
    @Transactional
    public HabilidadEmpleadoDto agregar(Long idEmpleado, AgregarHabilidadRequest datos) {
        Empleado empleado = empleados.findById(idEmpleado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado " + idEmpleado + "."));
        Habilidad habilidad = catalogo.findById(datos.habilidadId())
                .orElseThrow(() -> new DatosInvalidosException("habilidadId", "La habilidad no existe en el catálogo."));
        if (empleado.tieneHabilidad(habilidad)) {
            throw new DatosInvalidosException("habilidadId", "Ya tienes " + habilidad.getNombre() + " en tu perfil.");
        }
        String nivel = datos.nivelDeclarado() == null || datos.nivelDeclarado().isBlank() ? null
                : datos.nivelDeclarado().trim();
        HabilidadEmpleado nueva = empleado.agregarHabilidad(habilidad, nivel, LocalDate.now(reloj));
        return HabilidadEmpleadoDto.de(habilidadesEmpleado.save(nueva));
    }

    /** Bandeja del manager: habilidades de su equipo en un estado (por defecto, pendientes). */
    @Transactional(readOnly = true)
    public List<HabilidadEmpleadoDto> deMiEquipo(Long idManager, EstadoHabilidad estado) {
        List<HabilidadEmpleado> lista = estado == null
                ? habilidadesEmpleado.findByEmpleadoManagerActualIdOrderByFechaAgregadoDesc(idManager)
                : habilidadesEmpleado.findByEmpleadoManagerActualIdAndEstadoOrderByFechaAgregadoAsc(idManager, estado);
        return lista.stream().map(HabilidadEmpleadoDto::de).toList();
    }

    /**
     * RF-07 / RN-19: el manager ACTUAL del empleado avala o rechaza la habilidad.
     * Cualquier otro manager recibe 403, aunque tenga el rol MANAGER.
     */
    @Transactional
    public HabilidadEmpleadoDto revisar(Long idManager, Long idHabilidadEmpleado, EstadoHabilidad nuevoEstado) {
        HabilidadEmpleado habilidad = habilidadesEmpleado.findById(idHabilidadEmpleado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la habilidad " + idHabilidadEmpleado + "."));
        Manager manager = managers.findById(idManager)
                .orElseThrow(() -> new AccesoDenegadoException("Solo un manager puede revisar habilidades."));
        if (!habilidad.getEmpleado().esManejadoPor(manager)) {
            throw new AccesoDenegadoException("Solo el manager actual del empleado puede revisar sus habilidades.");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        switch (nuevoEstado) {
            case AVALADO -> manager.avalarHabilidad(habilidad, ahora);
            case RECHAZADO -> manager.rechazarHabilidad(habilidad, ahora);
            default -> throw new DatosInvalidosException("estado", "El estado debe ser AVALADO o RECHAZADO.");
        }
        HabilidadEmpleado guardada = habilidadesEmpleado.save(habilidad);
        if (nuevoEstado == EstadoHabilidad.AVALADO) {
            // HU-005: cambio en las habilidades avaladas -> se actualizan sus postulaciones activas
            recalculo.recalcularPostulacionesActivas(habilidad.getEmpleado());
        }
        return HabilidadEmpleadoDto.de(guardada);
    }
}
