package co.edu.javeriana.talentmatch.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas de los metodos de dominio del diagrama de clases, sin Spring ni base de datos. */
class ModeloDominioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 9, 0);

    private Manager manager;
    private Empleado empleado;
    private Habilidad docker;

    @BeforeEach
    void preparar() {
        manager = new Manager("Ana Manager", "ana@empresa.com", "hash");
        manager.setId(1L);
        empleado = new Empleado("Juan Empleado", "juan@empresa.com", "hash");
        empleado.setId(2L);
        empleado.setManagerActual(manager);
        docker = new Habilidad(10L, "Docker");
    }

    @Test
    void laHerenciaSigueElDiagrama() {
        assertInstanceOf(Empleado.class, manager);
        assertInstanceOf(Usuario.class, empleado);
        assertInstanceOf(Usuario.class, new AnalistaRRHH("R", "r@empresa.com", "h"));
        assertEquals(Rol.MANAGER, manager.getRol());
        assertEquals(Rol.EMPLEADO, empleado.getRol());
        assertEquals(Rol.LIDER_EJECUTIVO, new LiderEjecutivo("L", "l@empresa.com", "h").getRol());
        assertEquals(Rol.ADMIN, new Administrador("A", "a@empresa.com", "h").getRol());
    }

    @Test
    void unaHabilidadNuevaNaceAgregadaYNoCuenta() {
        HabilidadEmpleado he = empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());

        assertEquals(EstadoHabilidad.AGREGADO, he.getEstado());
        assertFalse(he.cuentaParaCalculo());
        assertTrue(empleado.habilidadesAvaladas().isEmpty());
    }

    @Test
    void avalarCambiaElEstadoYRegistraLaFechaYQuienAvalo() {
        HabilidadEmpleado he = empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());

        manager.avalarHabilidad(he, AHORA);

        assertEquals(EstadoHabilidad.AVALADO, he.getEstado());
        assertEquals(AHORA, he.getFechaAval());
        assertEquals(manager, he.getAvaladoPor());
        assertEquals(List.of(docker), empleado.habilidadesAvaladas());
    }

    @Test
    void noSePuedeAvalarDosVeces() {
        HabilidadEmpleado he = empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());
        he.avalar(manager, AHORA);

        assertThrows(ReglaNegocioException.class, () -> he.avalar(manager, AHORA));
    }

    @Test
    void soloElManagerActualPuedeAvalar() {
        Manager otro = new Manager("Otro", "otro@empresa.com", "hash");
        otro.setId(99L);
        HabilidadEmpleado he = empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());

        assertThrows(ReglaNegocioException.class, () -> otro.avalarHabilidad(he, AHORA));
        assertEquals(EstadoHabilidad.AGREGADO, he.getEstado());
    }

    @Test
    void unaHabilidadRechazadaNoCuenta() {
        HabilidadEmpleado he = empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());

        manager.rechazarHabilidad(he, AHORA);

        assertEquals(EstadoHabilidad.RECHAZADO, he.getEstado());
        assertFalse(he.cuentaParaCalculo());
    }

    @Test
    void noSeDuplicaUnaHabilidadEnElPerfil() {
        empleado.agregarHabilidad(docker, "Avanzado", AHORA.toLocalDate());

        assertThrows(ReglaNegocioException.class,
                () -> empleado.agregarHabilidad(new Habilidad("docker"), "Basico", AHORA.toLocalDate()));
    }

    @Test
    void vacanteSeActivaYSeCierra() {
        Vacante v = new Vacante("Backend", Senioridad.SENIOR, "Backend", manager, AHORA);
        assertEquals(EstadoVacante.BORRADOR, v.getEstado());

        v.activar(AHORA);
        assertTrue(v.estaActiva());
        assertEquals(AHORA, v.getFechaActivacion());

        v.cerrar(AHORA.plusDays(5));
        assertEquals(EstadoVacante.CERRADA, v.getEstado());
        assertThrows(ReglaNegocioException.class, () -> v.cerrar(AHORA));
    }

    @Test
    void vacanteConActivacionDiferidaEsperaLaVentana() {
        Vacante v = new Vacante("Backend", Senioridad.SENIOR, "Backend", manager, AHORA);
        v.programarActivacion(AHORA.plusHours(24));

        assertEquals(EstadoVacante.PENDIENTE_ACTIVACION, v.getEstado());
        assertFalse(v.debeActivarse(AHORA.plusHours(23)));
        assertTrue(v.debeActivarse(AHORA.plusHours(24)));
    }

    @Test
    void postulacionSeAceptaOSeRechazaUnaSolaVez() {
        Vacante v = new Vacante("Backend", Senioridad.SENIOR, "Backend", manager, AHORA);
        Postulacion p = new Postulacion(empleado, v, AHORA);
        assertEquals(EstadoPostulacion.ACTIVA, p.getEstado());

        p.aceptar(AHORA);
        assertEquals(EstadoPostulacion.ACEPTADA, p.getEstado());
        assertThrows(ReglaNegocioException.class, () -> p.rechazar(AHORA));

        Postulacion q = new Postulacion(empleado, v, AHORA);
        q.rechazar(AHORA);
        assertEquals(EstadoPostulacion.RECHAZADA, q.getEstado());
        assertEquals(AHORA, q.getFechaResolucion());
    }

    @Test
    void puntajeFinalEsIgualAlMatchScore() {
        Postulacion p = new Postulacion(empleado, new Vacante("B", Senioridad.SENIOR, "B", manager, AHORA), AHORA);
        p.registrarPuntaje(72.5);

        assertEquals(72.5, p.getPuntajeFinal());
        assertTrue(p.esViable(65));
        assertFalse(p.esViable(80));
    }

    @Test
    void elRankingFiltraPorUmbralYOrdenaDeMayorAMenor() {
        Vacante v = new Vacante("B", Senioridad.SENIOR, "B", manager, AHORA);
        Postulacion alta = postulacionCon(v, 85, AHORA);
        Postulacion media = postulacionCon(v, 72, AHORA);
        Postulacion baja = postulacionCon(v, 58, AHORA);

        List<Postulacion> ranking = Vacante.calcularRanking(List.of(media, baja, alta), 65);

        assertEquals(List.of(alta, media), ranking);
    }

    @Test
    void elMismoPuestoEsMismoProyectoYMismoTitulo() {
        Proyecto p = new Proyecto("Pagos", "Backend");
        p.setId(5L);
        Vacante a = new Vacante("Backend Senior", Senioridad.SENIOR, "Backend", manager, AHORA);
        a.setProyecto(p);
        Vacante b = new Vacante("backend senior", Senioridad.SENIOR, "Backend", manager, AHORA);
        b.setProyecto(p);
        Vacante c = new Vacante("QA", Senioridad.SENIOR, "Backend", manager, AHORA);
        c.setProyecto(p);

        assertTrue(a.esMismoPuestoQue(b));
        assertFalse(a.esMismoPuestoQue(c));
    }

    @Test
    void lasHabilidadesRequeridasUnenCriticasYDeseables() {
        Vacante v = new Vacante("B", Senioridad.SENIOR, "B", manager, AHORA);
        Habilidad go = new Habilidad(1L, "Go");
        v.setHabilidadesCriticas(Set.of(go, docker));
        v.setHabilidadesDeseables(Set.of(docker, new Habilidad(3L, "Kubernetes")));

        assertEquals(3, v.habilidadesRequeridas().size());
    }

    @Test
    void liberarUnaAsignacionLaCierra() {
        AsignacionProyecto a = new AsignacionProyecto(empleado, new Proyecto("Pagos", "Backend"), "Dev",
                LocalDate.of(2025, 1, 1));
        empleado.agregarAsignacion(a);
        assertTrue(empleado.asignacionActual().isPresent());

        a.liberar(MotivoFinAsignacion.RENUNCIA, LocalDate.of(2026, 1, 1));

        assertFalse(a.estaVigente());
        assertTrue(empleado.asignacionActual().isEmpty());
        assertEquals(OrigenVacante.RENUNCIA, a.getMotivoFin().origenVacante());
        assertThrows(ReglaNegocioException.class,
                () -> a.liberar(MotivoFinAsignacion.RENUNCIA, LocalDate.of(2026, 1, 2)));
    }

    @Test
    void laActivacionDiferidaSoloAplicaALaRotacionAceptada() {
        assertTrue(OrigenVacante.ROTACION_ACEPTADA.requiereActivacionDiferida());
        assertFalse(OrigenVacante.RENUNCIA.requiereActivacionDiferida());
        assertFalse(OrigenVacante.MANUAL.requiereActivacionDiferida());
    }

    @Test
    void alertaSeResuelveUnaVez() {
        AnalistaRRHH analista = new AnalistaRRHH("R", "r@empresa.com", "h");
        Alerta alerta = new Alerta(TipoAlerta.RECHAZOS_REPETIDOS, manager, null, "2 rechazos", AHORA);
        assertNull(alerta.getAtendidaPor());

        analista.revisarAlerta(alerta, AHORA);

        assertEquals(EstadoAlerta.RESUELTA, alerta.getEstado());
        assertEquals(analista, alerta.getAtendidaPor());
        assertThrows(ReglaNegocioException.class, () -> alerta.resolver(analista, AHORA));
    }

    @Test
    void antiguedadMinimaSeCalculaDesdeElInicioDelRol() {
        empleado.setFechaInicioRol(LocalDate.of(2026, 5, 15));

        assertEquals(LocalDate.of(2026, 11, 15), empleado.fechaHabilitacionPorAntiguedad(6));
    }

    private Postulacion postulacionCon(Vacante v, double puntaje, LocalDateTime fecha) {
        Postulacion p = new Postulacion(empleado, v, fecha);
        p.registrarPuntaje(puntaje);
        return p;
    }
}
