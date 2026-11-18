package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.javeriana.talentmatch.config.AlertasProperties;
import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.config.PlazosProperties;
import co.edu.javeriana.talentmatch.config.ReporteEjecutivoProperties;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.ElegibilidadDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.PostulacionCreadaDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.Proyecto;
import co.edu.javeriana.talentmatch.model.ReglaNegocioException;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.BitacoraCalculoRepository;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** HU-008: las tres reglas de postulacion y la cancelacion en cascada. */
class PostulacionServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final LocalDateTime AHORA = HOY.atTime(10, 0);

    private PostulacionRepository postulaciones;
    private VacanteRepository vacantes;
    private PostulacionService servicio;
    private Empleado empleado;
    private Manager manager;
    private Proyecto proyecto;
    private final List<Postulacion> guardadas = new ArrayList<>();

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        AlgoritmoProperties algoritmo = new AlgoritmoProperties(0.7, 0.3, 65, 0.9, List.of(1.0, 0.6, 0.3, 0.0));
        ParametrosService parametros = new ParametrosService(algoritmo, new PlazosProperties(6, 6, 24, 3, 10),
                new AlertasProperties(2), new ReporteEjecutivoProperties(10));
        BitacoraCalculoRepository bitacora = mock(BitacoraCalculoRepository.class);
        when(bitacora.save(any())).thenAnswer(i -> i.getArgument(0));
        postulaciones = mock(PostulacionRepository.class);
        when(postulaciones.save(any())).thenAnswer(i -> {
            Postulacion p = i.getArgument(0);
            if (p.getId() == null) {
                p.setId((long) (guardadas.size() + 100));
                guardadas.add(p);
            }
            return p;
        });
        EmpleadoRepository empleados = mock(EmpleadoRepository.class);
        vacantes = mock(VacanteRepository.class);
        MatchingService matching = new MatchingService(new CalculadoraMatchScore(algoritmo), bitacora, empleados,
                vacantes, parametros, reloj);
        servicio = new PostulacionService(postulaciones, empleados, vacantes, matching, parametros, reloj);

        manager = new Manager("M", "m@talentmatch.com", "h");
        manager.setId(1L);
        empleado = new Empleado("E", "e@talentmatch.com", "h");
        empleado.setId(2L);
        empleado.setSenioridad(Senioridad.SENIOR);
        empleado.setFechaInicioRol(HOY.minusMonths(8));
        proyecto = new Proyecto("Pagos", "Backend");
        proyecto.setId(9L);
        when(empleados.findById(2L)).thenReturn(Optional.of(empleado));
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of());
    }

    private Vacante vacante(long id, String titulo) {
        Vacante v = new Vacante(titulo, Senioridad.SENIOR, "Backend", manager, AHORA.minusDays(30));
        v.setId(id);
        v.setProyecto(proyecto);
        v.setHabilidadesCriticas(Set.of(new Habilidad(1L, "Java")));
        v.activar(AHORA.minusDays(30));
        when(vacantes.findById(id)).thenReturn(Optional.of(v));
        return v;
    }

    @Test
    void regla1_conMenosDe6MesesEnElRolNoPuedePostularseYSabeDesdeCuando() {
        empleado.setFechaInicioRol(HOY.minusMonths(4));
        vacante(10L, "Backend");

        ElegibilidadDto e = servicio.elegibilidad(2L, 10L);

        assertFalse(e.puedePostularse());
        assertEquals(HOY.plusMonths(2), e.fechaHabilitacion());
        assertTrue(e.motivo().startsWith("Podrás postularte a partir del 15 de diciembre de 2026"), e.motivo());
        assertTrue(e.motivo().contains("se requieren 6 meses en el rol actual"));
        assertThrows(ReglaNegocioException.class, () -> servicio.postularse(2L, 10L));
    }

    @Test
    void regla1_conExactamente6MesesSiPuede() {
        empleado.setFechaInicioRol(HOY.minusMonths(6));
        vacante(10L, "Backend");
        assertTrue(servicio.elegibilidad(2L, 10L).puedePostularse());
    }

    @Test
    void regla2_rechazadoHaceMenosDe6MesesEnElMismoPuestoQuedaBloqueado() {
        Vacante anterior = vacante(10L, "Backend Senior");
        Postulacion rechazo = new Postulacion(empleado, anterior, AHORA.minusMonths(3));
        rechazo.rechazar(AHORA.minusMonths(2));
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of(rechazo));
        vacante(11L, "Backend Senior"); // nueva rotacion del mismo puesto

        ElegibilidadDto e = servicio.elegibilidad(2L, 11L);

        assertFalse(e.puedePostularse());
        assertEquals(HOY.minusMonths(2).plusMonths(6), e.fechaHabilitacion());
    }

    @Test
    void regla2_pasados6MesesYConNuevaRotacionSiPuede() {
        Vacante anterior = vacante(10L, "Backend Senior");
        Postulacion rechazo = new Postulacion(empleado, anterior, AHORA.minusMonths(8));
        rechazo.rechazar(AHORA.minusMonths(7));
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of(rechazo));
        vacante(11L, "Backend Senior");

        assertTrue(servicio.elegibilidad(2L, 11L).puedePostularse());
    }

    @Test
    void regla2_sinNuevaRotacionNuncaPuedeRepostularseALaMismaVacante() {
        Vacante anterior = vacante(10L, "Backend Senior");
        Postulacion rechazo = new Postulacion(empleado, anterior, AHORA.minusMonths(9));
        rechazo.rechazar(AHORA.minusMonths(8));
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of(rechazo));

        ElegibilidadDto e = servicio.elegibilidad(2L, 10L);
        assertFalse(e.puedePostularse());
        assertTrue(e.motivo().contains("nueva rotación"));
    }

    @Test
    void regla2_unRechazoEnOtroPuestoNoBloquea() {
        Vacante otra = vacante(10L, "QA");
        Postulacion rechazo = new Postulacion(empleado, otra, AHORA.minusMonths(1));
        rechazo.rechazar(AHORA.minusDays(10));
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of(rechazo));
        vacante(11L, "Backend Senior");

        assertTrue(servicio.elegibilidad(2L, 11L).puedePostularse());
    }

    @Test
    void escenario2_unPuntajeBajoNoBloqueaPeroAvisa() {
        vacante(10L, "Backend"); // el empleado no tiene Java avalado -> 30 %

        PostulacionCreadaDto creada = servicio.postularse(2L, 10L);

        assertEquals(EstadoPostulacion.ACTIVA, creada.postulacion().estado());
        assertFalse(creada.viable());
        assertTrue(creada.aviso().contains("posibilidades son bajas"));
    }

    @Test
    void regla3_alAceptarUnaLasDemasSeCancelanEnCascada() {
        Postulacion a = new Postulacion(empleado, vacante(10L, "A"), AHORA);
        a.setId(1L);
        Postulacion b = new Postulacion(empleado, vacante(11L, "B"), AHORA);
        b.setId(2L);
        Postulacion c = new Postulacion(empleado, vacante(12L, "C"), AHORA);
        c.setId(3L);
        when(postulaciones.findByEmpleadoIdAndEstado(2L, EstadoPostulacion.ACTIVA)).thenReturn(List.of(a, b, c));

        a.aceptar(AHORA);
        List<Postulacion> canceladas = servicio.cancelarOtrasPostulaciones(a, AHORA);

        assertEquals(List.of(b, c), canceladas);
        assertEquals(EstadoPostulacion.CANCELADA_POR_ROTACION, b.getEstado());
        assertEquals(EstadoPostulacion.CANCELADA_POR_ROTACION, c.getEstado());
        assertEquals(EstadoPostulacion.ACEPTADA, a.getEstado());
    }

    @Test
    void noPuedePostularseASuPropiaVacante() {
        Vacante propia = vacante(10L, "Backend");
        propia.setManager(manager);
        when(postulaciones.findByEmpleadoIdAndEstado(1L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of());
        manager.setFechaInicioRol(HOY.minusYears(2));

        assertTrue(servicio.revisarReglas(manager, propia, false).isPresent());
    }
}
