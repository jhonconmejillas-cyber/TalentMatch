package co.edu.javeriana.talentmatch.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Plan de pruebas — Componente 2: Vacante (ciclo de vida RN-02/RN-03, mismo puesto RN-10,
 * ranking RN-14/RN-27). Pruebas de dominio puro, sin base de datos (F.I.R.S.T).
 */
@Tag("plan-pruebas")
@DisplayName("Componente 2 · Vacante")
class VacantePlanTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 18, 10, 0);
    private static final double UMBRAL = 65;

    private Manager manager;
    private Proyecto pagos;
    private Vacante vacante;

    @BeforeEach
    void preparar() {
        manager = new Manager("Carlos", "carlos@talentmatch.com", "h");
        manager.setId(4L);
        pagos = new Proyecto("Pagos", "Backend");
        pagos.setId(1L);
        vacante = nuevaVacante("Backend Senior", pagos);
    }

    private Vacante nuevaVacante(String titulo, Proyecto proyecto) {
        Vacante v = new Vacante(titulo, Senioridad.SENIOR, "Backend", manager, AHORA.minusDays(1));
        v.setProyecto(proyecto);
        return v;
    }

    private Postulacion postulacion(double puntaje, LocalDateTime fecha) {
        Postulacion p = new Postulacion(new Empleado("E", "e@talentmatch.com", "h"), vacante, fecha);
        p.registrarPuntaje(puntaje);
        return p;
    }

    @Test
    @DisplayName("CP11 | Normal | activar: una vacante en borrador queda ACTIVA con su fecha de activacion")
    void cp11_activarDesdeBorrador() {
        // Act
        vacante.activar(AHORA);
        // Assert
        assertTrue(vacante.estaActiva());
        assertEquals(AHORA, vacante.getFechaActivacion());
    }

    @Test
    @DisplayName("CP12 | Negativa | cerrar: una vacante que no esta activa no se puede cerrar")
    void cp12_cerrarBorrador() {
        // Act / Assert
        assertThrows(ReglaNegocioException.class, () -> vacante.cerrar(AHORA));
        assertEquals(EstadoVacante.BORRADOR, vacante.getEstado());
    }

    @Test
    @DisplayName("CP13 | Negativa | programarActivacion: sin fecha se rechaza y la vacante sigue en borrador")
    void cp13_programarSinFecha() {
        // Act / Assert
        assertThrows(ReglaNegocioException.class, () -> vacante.programarActivacion(null));
        assertEquals(EstadoVacante.BORRADOR, vacante.getEstado());
    }

    @Test
    @DisplayName("CP14 | Borde | debeActivarse: justo al cumplirse las 24 h si, un segundo antes no (RN-03)")
    void cp14_ventanaDeActivacionExacta() {
        // Arrange
        LocalDateTime programada = AHORA.plusHours(24);
        vacante.programarActivacion(programada);
        // Act / Assert
        assertFalse(vacante.debeActivarse(programada.minusSeconds(1)));
        assertTrue(vacante.debeActivarse(programada));
    }

    @Test
    @DisplayName("CP15 | Borde | calcularRanking: un Puntaje Final igual al umbral (65) entra y 64.9 no")
    void cp15_umbralExacto() {
        // Arrange
        Postulacion enElUmbral = postulacion(65.0, AHORA);
        Postulacion justoDebajo = postulacion(64.9, AHORA);
        // Act
        List<Postulacion> ranking = Vacante.calcularRanking(List.of(justoDebajo, enElUmbral), UMBRAL);
        // Assert
        assertEquals(List.of(enElUmbral), ranking);
    }

    @Test
    @DisplayName("CP16 | Negocio | calcularRanking: orden descendente, empate por fecha y sin postulaciones resueltas")
    void cp16_ordenDelRanking() {
        // Arrange
        Postulacion primera = postulacion(80, AHORA.minusDays(3));
        Postulacion empatadaAntes = postulacion(72, AHORA.minusDays(2));
        Postulacion empatadaDespues = postulacion(72, AHORA.minusDays(1));
        Postulacion rechazada = postulacion(95, AHORA.minusDays(4));
        rechazada.rechazar(AHORA);
        // Act
        List<Postulacion> ranking = Vacante.calcularRanking(
                List.of(empatadaDespues, rechazada, primera, empatadaAntes), UMBRAL);
        // Assert
        assertEquals(List.of(primera, empatadaAntes, empatadaDespues), ranking);
    }

    @Test
    @DisplayName("CP17 | Normal | esMismoPuestoQue: mismo proyecto y titulo sin importar mayusculas (RN-10)")
    void cp17_mismoPuesto() {
        // Arrange
        Vacante nuevaRotacion = nuevaVacante("backend senior", pagos);
        Vacante otroProyecto = nuevaVacante("Backend Senior", new Proyecto("Ventas", "Backend"));
        // Act / Assert
        assertTrue(vacante.esMismoPuestoQue(nuevaRotacion));
        assertFalse(vacante.esMismoPuestoQue(otroProyecto));
        assertFalse(vacante.esMismoPuestoQue(null));
    }

    @Test
    @DisplayName("CP18 | Negativa | descartar: una vacante ya cerrada no se puede descartar")
    void cp18_descartarCerrada() {
        // Arrange
        vacante.activar(AHORA);
        vacante.cerrar(AHORA);
        // Act / Assert
        assertThrows(ReglaNegocioException.class, () -> vacante.descartar(AHORA));
        assertEquals(EstadoVacante.CERRADA, vacante.getEstado());
    }
}
