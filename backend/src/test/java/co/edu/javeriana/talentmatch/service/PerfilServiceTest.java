package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.javeriana.talentmatch.dto.PerfilDtos.ActualizarPerfilRequest;
import co.edu.javeriana.talentmatch.dto.PerfilDtos.PerfilDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Proyecto;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.ProyectoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba unitaria del servicio, sin base de datos (repositorios simulados con Mockito). */
class PerfilServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    private EmpleadoRepository empleados;
    private ProyectoRepository proyectos;
    private PerfilService servicio;
    private Empleado empleado;
    private Proyecto pagos;

    @BeforeEach
    void preparar() {
        empleados = mock(EmpleadoRepository.class);
        proyectos = mock(ProyectoRepository.class);
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        servicio = new PerfilService(empleados, proyectos, reloj);

        empleado = new Empleado("Ana", "ana@talentmatch.com", "hash");
        empleado.setId(1L);
        pagos = new Proyecto("Pagos", "Backend");
        pagos.setId(10L);
        when(empleados.findById(1L)).thenReturn(Optional.of(empleado));
        when(empleados.save(any())).thenAnswer(i -> i.getArgument(0));
        when(proyectos.findById(10L)).thenReturn(Optional.of(pagos));
    }

    private ActualizarPerfilRequest datos(LocalDate inicioRol, Long proyecto, String cargo) {
        return new ActualizarPerfilRequest(Senioridad.SENIOR, 7, " Backend ", inicioRol,
                LocalDate.of(2020, 1, 1), proyecto, cargo);
    }

    @Test
    void actualizaLosDatosYDeclaraElProyectoActual() {
        PerfilDto perfil = servicio.actualizar(1L, datos(LocalDate.of(2025, 1, 1), 10L, "Backend Developer"));

        assertEquals(Senioridad.SENIOR, perfil.senioridad());
        assertEquals("Backend", perfil.area());
        assertEquals("Pagos", perfil.proyectoActual().nombre());
        assertEquals("Backend Developer", perfil.cargo());
        assertEquals(LocalDate.of(2025, 1, 1), empleado.asignacionActual().orElseThrow().getFechaInicio());
    }

    @Test
    void laFechaDeInicioDelRolNoSePuedeCambiarUnaVezDeclarada() {
        empleado.setFechaInicioRol(LocalDate.of(2026, 6, 1));

        DatosInvalidosException e = assertThrows(DatosInvalidosException.class,
                () -> servicio.actualizar(1L, datos(LocalDate.of(2020, 1, 1), null, null)));
        assertEquals("fechaInicioRol", e.getCampos().keySet().iterator().next());
    }

    @Test
    void noSeAceptanFechasFuturas() {
        assertThrows(DatosInvalidosException.class,
                () -> servicio.actualizar(1L, datos(HOY.plusDays(1), null, null)));
    }

    @Test
    void elProyectoActualNoCambiaDesdeElPerfil() {
        servicio.actualizar(1L, datos(LocalDate.of(2025, 1, 1), 10L, "Dev"));
        Proyecto otro = new Proyecto("Cloud", "Cloud");
        otro.setId(20L);
        when(proyectos.findById(20L)).thenReturn(Optional.of(otro));

        assertThrows(DatosInvalidosException.class,
                () -> servicio.actualizar(1L, datos(null, 20L, "Dev")));
    }

    @Test
    void declararProyectoExigeCargo() {
        assertThrows(DatosInvalidosException.class,
                () -> servicio.actualizar(1L, datos(LocalDate.of(2025, 1, 1), 10L, " ")));
    }
}
