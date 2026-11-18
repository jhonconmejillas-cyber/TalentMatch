package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.PerfilDtos.ActualizarPerfilRequest;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.PerfilDto;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.ProyectoDto;
import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Proyecto;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.ProyectoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Perfil profesional del empleado (RF-04, CU_03). */
@Service
public class PerfilService {

    private final EmpleadoRepository empleados;
    private final ProyectoRepository proyectos;
    private final Clock reloj;

    public PerfilService(EmpleadoRepository empleados, ProyectoRepository proyectos, Clock reloj) {
        this.empleados = empleados;
        this.proyectos = proyectos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public PerfilDto consultar(Long idEmpleado) {
        return aDto(obtener(idEmpleado));
    }

    @Transactional
    public PerfilDto actualizar(Long idEmpleado, ActualizarPerfilRequest datos) {
        Empleado empleado = obtener(idEmpleado);
        LocalDate hoy = LocalDate.now(reloj);

        empleado.setSenioridad(datos.senioridad());
        empleado.setAniosExperiencia(datos.aniosExperiencia());
        empleado.setArea(datos.area().trim());

        // Las fechas se declaran una sola vez; luego las mantiene el sistema (RN-07, RN-15).
        empleado.setFechaIngresoEmpresa(fijarUnaVez("fechaIngresoEmpresa", empleado.getFechaIngresoEmpresa(),
                datos.fechaIngresoEmpresa(), hoy, "la fecha de ingreso a la empresa"));
        empleado.setFechaInicioRol(fijarUnaVez("fechaInicioRol", empleado.getFechaInicioRol(),
                datos.fechaInicioRol(), hoy, "la fecha de inicio en tu rol actual"));
        if (empleado.getFechaIngresoEmpresa() != null && empleado.getFechaInicioRol() != null
                && empleado.getFechaInicioRol().isBefore(empleado.getFechaIngresoEmpresa())) {
            throw new DatosInvalidosException("fechaInicioRol",
                    "La fecha de inicio en el rol no puede ser anterior a tu ingreso a la empresa.");
        }

        actualizarProyecto(empleado, datos, hoy);
        return aDto(empleados.save(empleado));
    }

    @Transactional(readOnly = true)
    public List<ProyectoDto> proyectosActivos() {
        return proyectos.findAll().stream().filter(Proyecto::estaActivo)
                .map(ProyectoDto::de).toList();
    }

    /**
     * El proyecto actual se declara si el empleado aun no tiene asignacion.
     * Si ya la tiene, solo cambia al aceptarse una rotacion (RN-15), no desde el perfil.
     */
    private void actualizarProyecto(Empleado empleado, ActualizarPerfilRequest datos, LocalDate hoy) {
        Optional<AsignacionProyecto> actual = empleado.asignacionActual();
        if (datos.proyectoActualId() == null) {
            return;
        }
        if (actual.isPresent()) {
            if (!Objects.equals(actual.get().getProyecto().getId(), datos.proyectoActualId())) {
                throw new DatosInvalidosException("proyectoActualId",
                        "Tu proyecto actual solo cambia cuando se acepta una rotación.");
            }
            return;
        }
        if (datos.cargo() == null || datos.cargo().isBlank()) {
            throw new DatosInvalidosException("cargo", "Escribe tu cargo en el proyecto (por ejemplo: Backend Developer).");
        }
        Proyecto proyecto = proyectos.findById(datos.proyectoActualId())
                .filter(Proyecto::estaActivo)
                .orElseThrow(() -> new DatosInvalidosException("proyectoActualId", "Selecciona un proyecto activo."));
        LocalDate inicio = empleado.getFechaInicioRol() != null ? empleado.getFechaInicioRol() : hoy;
        empleado.agregarAsignacion(new AsignacionProyecto(empleado, proyecto, datos.cargo().trim(), inicio));
    }

    private static LocalDate fijarUnaVez(String campo, LocalDate actual, LocalDate nueva, LocalDate hoy, String nombre) {
        if (nueva == null || nueva.equals(actual)) {
            return actual;
        }
        if (actual != null) {
            throw new DatosInvalidosException(campo, "No puedes cambiar " + nombre + "; pídele el ajuste a RR.HH.");
        }
        if (nueva.isAfter(hoy)) {
            throw new DatosInvalidosException(campo, "La fecha no puede estar en el futuro.");
        }
        return nueva;
    }

    private Empleado obtener(Long id) {
        return empleados.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el perfil de empleado " + id + "."));
    }

    private static PerfilDto aDto(Empleado e) {
        Optional<AsignacionProyecto> asignacion = e.asignacionActual();
        return new PerfilDto(e.getId(), e.getNombre(), e.getCorreo(), e.getSenioridad(), e.getAniosExperiencia(),
                e.getArea(), e.getFechaInicioRol(), e.getFechaIngresoEmpresa(),
                asignacion.map(a -> ProyectoDto.de(a.getProyecto())).orElse(null),
                asignacion.map(AsignacionProyecto::getCargo).orElse(null),
                e.getManagerActual() == null ? null : e.getManagerActual().getNombre());
    }
}
