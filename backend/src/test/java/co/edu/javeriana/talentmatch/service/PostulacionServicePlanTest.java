package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.javeriana.talentmatch.config.AlertasProperties;
import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.config.PlazosProperties;
import co.edu.javeriana.talentmatch.config.ReporteEjecutivoProperties;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.ElegibilidadDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.MiPostulacionDto;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Plan de pruebas — Componente 3: PostulacionService (HU-008: RN-07, RN-09, RN-10, RN-28).
 * Los repositorios se reemplazan por mocks de Mockito y el reloj es fijo, asi cada prueba es
 * rapida, independiente y repetible (F.I.R.S.T).
 */
@Tag("plan-pruebas")
@DisplayName("Componente 3 · PostulacionService")
class PostulacionServicePlanTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime AHORA = HOY.atTime(10, 0);

    private PostulacionRepository postulaciones;
    private VacanteRepository vacantes;
    private PostulacionService servicio;
    private Empleado empleado;
    private Manager manager;
    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        AlgoritmoProperties algoritmo = new AlgoritmoProperties(0.7, 0.3, 65, 0.9, List.of(1.0, 0.6, 0.3, 0.0));
        ParametrosService parametros = new ParametrosService(algoritmo, new PlazosProperties(6, 6, 24, 3, 10),
                new AlertasProperties(2), new ReporteEjecutivoProperties(10));
        BitacoraCalculoRepository bitacora = mock(BitacoraCalculoRepository.class);
        when(bitacora.save(any())).thenAnswer(i -> i.getArgument(0));
        postulaciones = mock(PostulacionRepository.class);
        when(postulaciones.save(any())).thenAnswer(i -> i.getArgument(0));
        EmpleadoRepository empleados = mock(EmpleadoRepository.class);
        vacantes = mock(VacanteRepository.class);
        MatchingService matching = new MatchingService(new CalculadoraMatchScore(algoritmo), bitacora, empleados,
                vacantes, parametros, reloj);
        servicio = new PostulacionService(postulaciones, empleados, vacantes, matching, parametros, reloj);

        manager = new Manager("Carlos", "carlos@talentmatch.com", "h");
        manager.setId(4L);
        empleado = new Empleado("Sofia", "sofia@talentmatch.com", "h");
        empleado.setId(8L);
        empleado.setSenioridad(Senioridad.SENIOR);
        empleado.setFechaInicioRol(HOY.minusMonths(12));
        proyecto = new Proyecto("Pagos", "Backend");
        proyecto.setId(1L);
        when(empleados.findById(8L)).thenReturn(Optional.of(empleado));
        when(postulaciones.findByEmpleadoIdAndEstado(8L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of());
    }

    private Vacante vacanteActiva(long id, String titulo) {
        Vacante v = new Vacante(titulo, Senioridad.SENIOR, "Backend", manager, AHORA.minusDays(20));
        v.setId(id);
        v.setProyecto(proyecto);
        v.setHabilidadesCriticas(Set.of(new Habilidad(1L, "Java")));
        v.activar(AHORA.minusDays(20));
        when(vacantes.findById(id)).thenReturn(Optional.of(v));
        return v;
    }

    @Test
    @DisplayName("CP19 | Normal | postularse: crea una postulacion ACTIVA con su Puntaje Final y la guarda")
    void cp19_postulacionExitosa() {
        // Arrange
        vacanteActiva(10L, "Backend Senior");
        // Act
        PostulacionCreadaDto creada = servicio.postularse(8L, 10L);
        // Assert
        assertEquals(EstadoPostulacion.ACTIVA, creada.postulacion().estado());
        verify(postulaciones).save(any(Postulacion.class));
    }

    @Test
    @DisplayName("CP20 | Negativa | postularse: un empleado inexistente lanza RecursoNoEncontradoException")
    void cp20_empleadoInexistente() {
        // Arrange
        vacanteActiva(10L, "Backend Senior");
        // Act / Assert
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.postularse(999L, 10L));
        verify(postulaciones, never()).save(any());
    }

    @Test
    @DisplayName("CP21 | Negativa | postularse: repetir la postulacion a la misma vacante se rechaza")
    void cp21_postulacionRepetida() {
        // Arrange
        vacanteActiva(10L, "Backend Senior");
        when(postulaciones.existsByEmpleadoIdAndVacanteIdAndEstado(8L, 10L, EstadoPostulacion.ACTIVA))
                .thenReturn(true);
        // Act / Assert
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.postularse(8L, 10L));
        assertTrue(error.getMessage().contains("Ya tienes una postulación activa"));
    }

    @Test
    @DisplayName("CP22 | Borde | elegibilidad: con 6 meses menos un dia en el rol queda bloqueado (RN-07)")
    void cp22_unDiaAntesDeLosSeisMeses() {
        // Arrange
        empleado.setFechaInicioRol(HOY.minusMonths(6).plusDays(1));
        vacanteActiva(10L, "Backend Senior");
        // Act
        ElegibilidadDto e = servicio.elegibilidad(8L, 10L);
        // Assert
        assertFalse(e.puedePostularse());
        assertEquals(HOY.plusDays(1), e.fechaHabilitacion());
    }

    @Test
    @DisplayName("CP23 | Borde | elegibilidad: rechazado hace exactamente 6 meses, con nueva rotacion, ya puede (RN-10)")
    void cp23_seisMesesExactosTrasRechazo() {
        // Arrange
        Vacante anterior = vacanteActiva(10L, "Backend Senior");
        Postulacion rechazo = new Postulacion(empleado, anterior, AHORA.minusMonths(7));
        rechazo.rechazar(AHORA.minusMonths(6));
        when(postulaciones.findByEmpleadoIdAndEstado(8L, EstadoPostulacion.RECHAZADA)).thenReturn(List.of(rechazo));
        vacanteActiva(11L, "Backend Senior"); // nueva rotacion del mismo puesto
        // Act
        ElegibilidadDto e = servicio.elegibilidad(8L, 11L);
        // Assert
        assertTrue(e.puedePostularse(), e.motivo());
    }

    @Test
    @DisplayName("CP24 | Negocio | cancelarOtrasPostulaciones: al aceptar una, las demas pasan a CANCELADA_POR_ROTACION (RN-09)")
    void cp24_cancelacionEnCascada() {
        // Arrange
        Postulacion aceptada = new Postulacion(empleado, vacanteActiva(10L, "A"), AHORA);
        aceptada.setId(1L);
        Postulacion otra = new Postulacion(empleado, vacanteActiva(11L, "B"), AHORA);
        otra.setId(2L);
        when(postulaciones.findByEmpleadoIdAndEstado(8L, EstadoPostulacion.ACTIVA))
                .thenReturn(List.of(aceptada, otra));
        aceptada.aceptar(AHORA);
        // Act
        List<Postulacion> canceladas = servicio.cancelarOtrasPostulaciones(aceptada, AHORA);
        // Assert
        assertEquals(List.of(otra), canceladas);
        assertEquals(EstadoPostulacion.CANCELADA_POR_ROTACION, otra.getEstado());
        assertEquals(EstadoPostulacion.ACEPTADA, aceptada.getEstado());
    }

    @Test
    @DisplayName("CP25 | Negativa | elegibilidad: una vacante cerrada no recibe postulaciones")
    void cp25_vacanteCerrada() {
        // Arrange
        Vacante cerrada = vacanteActiva(10L, "Backend Senior");
        cerrada.cerrar(AHORA);
        // Act
        ElegibilidadDto e = servicio.elegibilidad(8L, 10L);
        // Assert
        assertFalse(e.puedePostularse());
        assertEquals("La vacante no está recibiendo postulaciones.", e.motivo());
    }

    @Test
    @DisplayName("CP26 | Normal | misPostulaciones: devuelve las postulaciones del empleado como DTO")
    void cp26_misPostulaciones() {
        // Arrange
        Postulacion p = new Postulacion(empleado, vacanteActiva(10L, "Backend Senior"), AHORA);
        p.setId(50L);
        when(postulaciones.findByEmpleadoIdOrderByFechaCreacionDesc(8L)).thenReturn(List.of(p));
        // Act
        List<MiPostulacionDto> mias = servicio.misPostulaciones(8L);
        // Assert
        assertEquals(1, mias.size());
        assertEquals(50L, mias.get(0).id());
        assertEquals("Backend Senior", mias.get(0).vacante());
    }
}
