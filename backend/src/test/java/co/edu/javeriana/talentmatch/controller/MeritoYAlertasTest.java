package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-004: competir por merito; historial de posicion y alerta automatica por rechazos (RN-29). */
class MeritoYAlertasTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    private long postular(long empleado, long vacante, double puntaje) {
        return jdbc.queryForObject("""
                INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final, fecha_creacion)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days') RETURNING id""",
                Long.class, empleado, vacante, puntaje, puntaje);
    }

    private int alertasDeRechazo() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM alerta WHERE tipo = 'RECHAZOS_REPETIDOS'", Integer.class);
    }

    @BeforeEach
    void preparar() {
        jdbc.update("UPDATE vacante SET fecha_activacion = CURRENT_TIMESTAMP - INTERVAL '10 days'");
    }

    @Test
    void escenario3_alSegundoRechazoViableSeAlertaARrhh() throws Exception {
        long a = postular(8, 1, 82);
        long b = postular(12, 1, 75);
        long c = postular(13, 1, 60); // no viable: no cuenta
        String carlos = login(MANAGER_CARLOS);

        mvc.perform(post("/api/v1/postulaciones/" + a + "/rechazar").header("Authorization", carlos))
                .andExpect(status().isOk());
        assertEquals(0, alertasDeRechazo(), "Con un solo rechazo todavia no hay alerta");

        mvc.perform(post("/api/v1/postulaciones/" + b + "/rechazar").header("Authorization", carlos))
                .andExpect(status().isOk());
        assertEquals(1, alertasDeRechazo());

        // RR.HH. ve la alerta con los puntajes comparados y la notificacion
        String rrhh = login(RRHH);
        mvc.perform(get("/api/v1/alertas?estado=ABIERTA").header("Authorization", rrhh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("RECHAZOS_REPETIDOS"))
                .andExpect(jsonPath("$[0].managerId").value(4))
                .andExpect(jsonPath("$[0].detalle", containsString("Empleado_008 con 82.0 %")))
                .andExpect(jsonPath("$[0].detalle", containsString("Empleado_012 con 75.0 %")));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacion WHERE destinatario_id = 2 AND tipo = 'ALERTA'", Integer.class));
        assertEquals("ACTIVA", jdbc.queryForObject("SELECT estado FROM postulacion WHERE id = ?", String.class, c));
    }

    @Test
    void cerrarLaVacanteConOtroCandidatoNoCuentaComoRechazoDirecto() throws Exception {
        long ganador = postular(11, 1, 90);
        postular(8, 1, 82);
        postular(12, 1, 75);

        mvc.perform(post("/api/v1/postulaciones/" + ganador + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk());
        assertEquals(0, alertasDeRechazo());
    }

    @Test
    void escenario2_elEmpleadoVeSuPosicionEnElHistorial() throws Exception {
        long sofia = postular(8, 1, 92);
        postular(11, 1, 90);
        long isabella = postular(12, 1, 75);

        mvc.perform(post("/api/v1/postulaciones/" + isabella + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/empleados/me/postulaciones").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(jsonPath("$[0].id").value(sofia))
                .andExpect(jsonPath("$[0].estado").value("RECHAZADA"))
                .andExpect(jsonPath("$[0].posicionRanking").value(1))
                .andExpect(jsonPath("$[0].totalCandidatos").value(3));
    }

    @Test
    void rrhhResuelveLaAlertaYSoloRrhhVeLaBandeja() throws Exception {
        Long id = jdbc.queryForObject("""
                INSERT INTO alerta (tipo, manager_id, detalle) VALUES ('RECHAZOS_REPETIDOS', 4, 'x') RETURNING id""",
                Long.class);
        mvc.perform(get("/api/v1/alertas").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/alertas/" + id).header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTA"));
    }
}
