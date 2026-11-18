package co.edu.javeriana.talentmatch.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.javeriana.talentmatch.model.AnalistaRRHH;
import co.edu.javeriana.talentmatch.model.BitacoraCalculo;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.model.Vacante;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de integracion del mapeo JPA contra PostgreSQL con las migraciones y los seeds.
 * {@code @Transactional} deshace todos los cambios al terminar cada prueba.
 */
@SpringBootTest
@Transactional
class MapeoJpaTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired EmpleadoRepository empleados;
    @Autowired HabilidadRepository habilidades;
    @Autowired VacanteRepository vacantes;
    @Autowired BitacoraCalculoRepository bitacora;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;

    @Test
    void laColumnaRolDefineLaSubclase() {
        assertInstanceOf(Manager.class, usuarios.findByCorreoIgnoreCase("carlos.perez@talentmatch.com").orElseThrow());
        assertInstanceOf(AnalistaRRHH.class, usuarios.findByCorreoIgnoreCase("rrhh@talentmatch.com").orElseThrow());
    }

    @Test
    void elEmpleadoCargaSuManagerYSusHabilidades() {
        Empleado juan = empleados.findById(7L).orElseThrow();

        assertEquals(Senioridad.SENIOR, juan.getSenioridad());
        assertEquals(4L, juan.getManagerActual().getId());
        assertEquals(4, juan.getHabilidades().size());
        assertEquals(3, juan.habilidadesAvaladas().size(), "Kubernetes esta AGREGADO y no cuenta");
        assertTrue(juan.asignacionActual().isPresent());
    }

    @Test
    void laVacanteCargaSusHabilidadesCriticasYDeseables() {
        Vacante backend = vacantes.findById(1L).orElseThrow();

        assertEquals(3, backend.getHabilidadesCriticas().size());
        assertEquals(2, backend.getHabilidadesDeseables().size());
        assertEquals(4L, backend.getManager().getId());
    }

    @Test
    void guardarUnEmpleadoLlenaLasTablasUsuarioYEmpleado() {
        Empleado nuevo = new Empleado("Prueba", "prueba.mapeo@talentmatch.com", "hash");
        nuevo.setSenioridad(Senioridad.JUNIOR);
        nuevo.setArea("Backend");
        nuevo.setFechaInicioRol(LocalDate.now());
        Habilidad java = habilidades.findByNombreIgnoreCase("java").orElseThrow();
        nuevo.agregarHabilidad(java, "Basico", LocalDate.now());

        Long id = empleados.saveAndFlush(nuevo).getId();

        assertEquals("EMPLEADO", jdbc.queryForObject("SELECT rol FROM usuario WHERE id = ?", String.class, id));
        assertEquals("Backend", jdbc.queryForObject("SELECT area FROM empleado WHERE id = ?", String.class, id));
        assertEquals("AGREGADO", jdbc.queryForObject(
                "SELECT estado FROM habilidad_empleado WHERE empleado_id = ?", String.class, id));
    }

    @Test
    void avalarSeGuardaEnLaBaseDeDatos() {
        Empleado juan = empleados.findById(7L).orElseThrow();
        HabilidadEmpleado kubernetes = juan.getHabilidades().stream()
                .filter(h -> h.getHabilidad().getNombre().equals("Kubernetes")).findFirst().orElseThrow();

        juan.getManagerActual().avalarHabilidad(kubernetes, LocalDateTime.now());
        em.flush();

        assertEquals("AVALADO", jdbc.queryForObject(
                "SELECT estado FROM habilidad_empleado WHERE id = ?", String.class, kubernetes.getId()));
    }

    @Test
    void laBaseDeDatosImpideModificarLaBitacora() {
        Empleado juan = empleados.findById(7L).orElseThrow();
        Vacante backend = vacantes.findById(1L).orElseThrow();
        BitacoraCalculo registro = bitacora.save(
                new BitacoraCalculo(juan, backend, 50, 50, 65, LocalDateTime.now(), "prueba"));
        em.flush();

        assertThrows(Exception.class, () -> jdbc.update(
                "UPDATE bitacora_calculo SET puntaje_final = 99 WHERE id = ?", registro.getId()));
    }
}
