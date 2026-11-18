package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-005: el puntaje guardado se recalcula solo cuando cambian los datos del calculo. */
class RecalculoTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager em;

    private double puntaje(long postulacion) {
        em.flush(); // la prueba comparte la transaccion: se escriben los cambios pendientes antes de leer
        return jdbc.queryForObject("SELECT puntaje_final FROM postulacion WHERE id = ?", Double.class, postulacion);
    }

    @Test
    void avalarUnaHabilidadActualizaLasPostulacionesActivas() throws Exception {
        // Juan postulado a Cloud Engineer (Kubernetes es critica); su Kubernetes esta AGREGADO
        long id = jdbc.queryForObject("""
                INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final)
                VALUES (7, 2, 10, 10) RETURNING id""", Long.class);
        Long kubernetes = jdbc.queryForObject(
                "SELECT id FROM habilidad_empleado WHERE empleado_id = 7 AND habilidad_id = 6", Long.class);
        int bitacoraAntes = jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class);

        mvc.perform(patch("/api/v1/habilidades-empleado/" + kubernetes).header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"AVALADO\"}"))
                .andExpect(status().isOk());

        assertTrue(puntaje(id) > 10, "Se recalculo con Kubernetes avalado: " + puntaje(id));
        assertEquals(bitacoraAntes + 1, jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class));
    }

    @Test
    void consultarElRankingNoRecalculaNiEscribeEnLaBitacora() throws Exception {
        jdbc.update("INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final) VALUES (11, 2, 90, 90)");
        int antes = jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class);

        mvc.perform(get("/api/v1/vacantes/2/ranking").header("Authorization", login(MANAGER_DIANA)))
                .andExpect(status().isOk());

        assertEquals(antes, jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class));
    }
}
