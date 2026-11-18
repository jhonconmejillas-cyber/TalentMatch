package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.model.OrigenVacante;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.AsignacionProyectoRepository;
import co.edu.javeriana.talentmatch.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** HU-009: integracion con PostgreSQL y los datos de prueba (se deshace al terminar). */
@SpringBootTest
@Transactional
class VacanteAutomaticaServiceTest {

    @Autowired VacanteAutomaticaService servicio;
    @Autowired AsignacionProyectoRepository asignaciones;
    @Autowired NotificacionRepository notificaciones;
    @Autowired JdbcTemplate jdbc;

    private final LocalDateTime ahora = LocalDateTime.now();

    private AsignacionProyecto asignacionDe(long empleado) {
        return asignaciones.findByEmpleadoIdAndFechaFinIsNull(empleado).orElseThrow();
    }

    @Test
    void escenario1_unaRenunciaCreaLaVacanteActivaDeInmediato() {
        Vacante v = servicio.liberarAsignacion(asignacionDe(8), MotivoFinAsignacion.RENUNCIA, ahora);

        assertEquals(EstadoVacante.ACTIVA, v.getEstado());
        assertEquals(OrigenVacante.RENUNCIA, v.getOrigen());
        assertEquals("Backend Developer", v.getTitulo());
        assertEquals(4L, v.getManager().getId(), "El solicitante es el manager de quien sale");
        assertEquals(4, v.getHabilidadesCriticas().size(), "Hereda las habilidades avaladas de Sofia");
    }

    @Test
    void escenario2_unaRotacionAceptadaSeActiva24HorasDespues() {
        Vacante v = servicio.liberarAsignacion(asignacionDe(8), MotivoFinAsignacion.ROTACION_ACEPTADA, ahora);
        assertEquals(EstadoVacante.PENDIENTE_ACTIVACION, v.getEstado());

        servicio.activarPendientes(ahora.plusHours(23));
        assertEquals(EstadoVacante.PENDIENTE_ACTIVACION, v.getEstado(), "No aparece antes de las 24 h");

        servicio.activarPendientes(ahora.plusHours(24));
        assertEquals(EstadoVacante.ACTIVA, v.getEstado());
    }

    @Test
    void unaCadenaDe3RotacionesGenera3VacantesIndependientes() {
        // A deja su puesto -> B lo toma dejando el suyo -> C toma el de B (RN-04)
        Vacante a = servicio.liberarAsignacion(asignacionDe(7), MotivoFinAsignacion.ROTACION_ACEPTADA, ahora);
        Vacante b = servicio.liberarAsignacion(asignacionDe(11), MotivoFinAsignacion.ROTACION_ACEPTADA, ahora.plusHours(1));
        Vacante c = servicio.liberarAsignacion(asignacionDe(13), MotivoFinAsignacion.ROTACION_ACEPTADA, ahora.plusHours(2));

        assertEquals(3, List.of(a.getId(), b.getId(), c.getId()).stream().distinct().count());
        assertEquals(3, servicio.activarPendientes(ahora.plusHours(26)));
        assertTrue(List.of(a, b, c).stream().allMatch(Vacante::estaActiva));
    }

    @Test
    void escenario3_unaVacanteEstancadaAlertaUnaSolaVezAlManagerYAlJefe() {
        int antes = notificaciones.findByTipo(TipoNotificacion.VACANTE_SIN_MATCH).size();
        int alertas = servicio.revisarVacantesSinMatch(ahora.plusDays(11));

        assertEquals(4, alertas, "Las 4 vacantes de prueba superan los 10 dias");
        assertEquals(0, servicio.revisarVacantesSinMatch(ahora.plusDays(12)), "No se repite la alerta");
        var nuevas = notificaciones.findByTipo(TipoNotificacion.VACANTE_SIN_MATCH);
        assertTrue(nuevas.size() > antes);
        assertTrue(nuevas.stream().anyMatch(n -> n.getDestinatario().getId() == 4L), "Manager solicitante");
        assertTrue(nuevas.stream().anyMatch(n -> n.getDestinatario().getId() == 2L), "Jefe del area / RR.HH.");
    }

    @Test
    void antesDelUmbralNoHayAlerta() {
        assertEquals(0, servicio.revisarVacantesSinMatch(ahora.plusDays(4)));
    }

    @Test
    void alActivarseAvisaAQuienTieneEsasHabilidadesEnSuWishlist() {
        // Santiago (11) tiene Kubernetes avalado; Juan (7) tiene Kubernetes en su wishlist
        servicio.liberarAsignacion(asignacionDe(11), MotivoFinAsignacion.RENUNCIA, ahora);

        assertTrue(notificaciones.findByDestinatarioIdOrderByFechaDesc(7L).stream()
                .anyMatch(n -> n.getTipo() == TipoNotificacion.VACANTE_WISHLIST));
    }
}
