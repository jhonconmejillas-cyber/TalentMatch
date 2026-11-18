package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.javeriana.talentmatch.config.AlertasProperties;
import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.config.PlazosProperties;
import co.edu.javeriana.talentmatch.config.ReporteEjecutivoProperties;
import co.edu.javeriana.talentmatch.dto.MatchingDtos.MiPuntajeDto;
import co.edu.javeriana.talentmatch.model.BitacoraCalculo;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.BitacoraCalculoRepository;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** CU_11: calculo del Puntaje Final con bitacora obligatoria. */
class MatchingServiceTest {

    private static final Habilidad JAVA = new Habilidad(1L, "Java");
    private static final Habilidad SQL = new Habilidad(3L, "SQL");
    private static final Habilidad DOCKER = new Habilidad(5L, "Docker");

    private BitacoraCalculoRepository bitacora;
    private MatchingService servicio;
    private Empleado empleado;
    private Manager manager;
    private Vacante vacante;

    @BeforeEach
    void preparar() {
        AlgoritmoProperties algoritmo = new AlgoritmoProperties(0.7, 0.3, 65, 0.9, List.of(1.0, 0.6, 0.3, 0.0));
        ParametrosService parametros = new ParametrosService(algoritmo, new PlazosProperties(6, 6, 24, 3, 10),
                new AlertasProperties(2), new ReporteEjecutivoProperties(10));
        bitacora = mock(BitacoraCalculoRepository.class);
        when(bitacora.save(any())).thenAnswer(i -> i.getArgument(0));
        EmpleadoRepository empleados = mock(EmpleadoRepository.class);
        VacanteRepository vacantes = mock(VacanteRepository.class);
        servicio = new MatchingService(new CalculadoraMatchScore(algoritmo), bitacora, empleados, vacantes,
                parametros, Clock.systemDefaultZone());

        manager = new Manager("M", "m@talentmatch.com", "h");
        manager.setId(1L);
        empleado = new Empleado("E", "e@talentmatch.com", "h");
        empleado.setId(2L);
        empleado.setManagerActual(manager);
        empleado.setSenioridad(Senioridad.SENIOR);
        vacante = new Vacante("Backend", Senioridad.SENIOR, "Backend", manager, LocalDateTime.now());
        vacante.setId(3L);
        vacante.setHabilidadesCriticas(Set.of(JAVA, SQL, DOCKER));
        when(empleados.findById(2L)).thenReturn(Optional.of(empleado));
        when(vacantes.findById(3L)).thenReturn(Optional.of(vacante));
    }

    private void avalar(Habilidad h) {
        HabilidadEmpleado he = empleado.agregarHabilidad(h, null, LocalDate.now());
        he.avalar(manager, LocalDateTime.now());
    }

    @Test
    void cadaCalculoDejaUnRegistroEnLaBitacora() {
        avalar(JAVA);
        avalar(SQL);

        MiPuntajeDto puntaje = servicio.miPuntaje(2L, 3L);

        ArgumentCaptor<BitacoraCalculo> registro = ArgumentCaptor.forClass(BitacoraCalculo.class);
        verify(bitacora).save(registro.capture());
        assertEquals(puntaje.puntajeFinal(), registro.getValue().getPuntajeFinal());
        assertEquals(puntaje.matchScore(), registro.getValue().getMatchScore());
        assertEquals(65.0, registro.getValue().getUmbralAplicado());
        assertEquals(empleado, registro.getValue().getEmpleado());
        assertEquals(vacante, registro.getValue().getVacante());
        assertNotNull(registro.getValue().getFechaHora());
    }

    @Test
    void siFallaLaBitacoraFallaElCalculo() {
        when(bitacora.save(any())).thenThrow(new IllegalStateException("base de datos caida"));

        assertThrows(IllegalStateException.class, () -> servicio.miPuntaje(2L, 3L));
    }

    @Test
    void unaHabilidadAgregadaNoCambiaElPuntaje() {
        avalar(JAVA);
        double antes = servicio.miPuntaje(2L, 3L).puntajeFinal();

        empleado.agregarHabilidad(SQL, "Experto", LocalDate.now()); // queda AGREGADO
        double despues = servicio.miPuntaje(2L, 3L).puntajeFinal();

        assertEquals(antes, despues);
    }

    @Test
    void puntajeFinalEsIgualAlMatchScore() {
        avalar(JAVA);
        MiPuntajeDto p = servicio.miPuntaje(2L, 3L);
        assertEquals(p.matchScore(), p.puntajeFinal());
    }

    @Test
    void avisaSiElPuntajeEstaPorDebajoDelUmbralSinBloquear() {
        avalar(JAVA); // 1/3 x 0.7 + 1 x 0.3 = 53.3
        MiPuntajeDto p = servicio.miPuntaje(2L, 3L);

        assertFalse(p.viable());
        assertTrue(p.aviso().contains("posibilidades son bajas"));
        assertTrue(p.aviso().contains("puedes postularte"));
    }

    @Test
    void noAvisaSiEsViable() {
        avalar(JAVA);
        avalar(SQL);
        avalar(DOCKER);
        MiPuntajeDto p = servicio.miPuntaje(2L, 3L);

        assertTrue(p.viable());
        assertNull(p.aviso());
        assertEquals(100.0, p.puntajeFinal());
    }
}
