package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-001: ranking de candidatos internos y decision del manager. */
class RankingControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    /** Postulacion a la vacante 1 (Backend Senior, manager Carlos) creada hace 2 dias, ya en el lote. */
    private long postular(long empleado, long vacante, double puntaje) {
        return jdbc.queryForObject("""
                INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final, fecha_creacion)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days') RETURNING id""",
                Long.class, empleado, vacante, puntaje, puntaje);
    }

    long santiago;
    long sofia;
    long isabella;
    long nicolas;
    long camila;

    @BeforeEach
    void preparar() {
        // La vacante 1 se activo hace 10 dias: el ultimo corte de ciclo (3 dias) fue hace 1 dia (RN-12).
        jdbc.update("UPDATE vacante SET fecha_activacion = CURRENT_TIMESTAMP - INTERVAL '10 days' WHERE id IN (1, 3)");
        santiago = postular(11, 1, 90);
        sofia = postular(8, 1, 86);
        isabella = postular(12, 1, 72);
        nicolas = postular(13, 1, 68);
        camila = postular(14, 1, 58);
    }

    @Test
    void escenario1_rankingOrdenadoSoloConViablesYConIndicador() throws Exception {
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatos.length()").value(4)) // Camila (58) no aparece
                .andExpect(jsonPath("$.candidatos[0].candidato").value("Empleado_011"))
                .andExpect(jsonPath("$.candidatos[0].posicion").value(1))
                .andExpect(jsonPath("$.candidatos[0].indicador").value("VERDE"))
                .andExpect(jsonPath("$.candidatos[2].indicador").value("AMARILLO"))
                .andExpect(jsonPath("$.candidatos[3].puntajeFinal").value(68.0))
                .andExpect(jsonPath("$.candidatos[3].indicador").value("ROJO"))
                .andExpect(jsonPath("$.candidatos[0].habilitadoParaDecidir").value(true))
                // Sin datos personales en el ranking
                .andExpect(jsonPath("$.candidatos[0].nombre").doesNotExist());
    }

    @Test
    void soloElManagerDeLaVacanteYRrhhVenElRanking() throws Exception {
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", login(MANAGER_DIANA)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", login(RRHH)))
                .andExpect(status().isOk());
    }

    @Test
    void escenario3_laApiRechazaCualquierReordenamiento() throws Exception {
        String body = "{\"orden\":[" + nicolas + "," + santiago + "]}";
        for (String token : new String[] {login(MANAGER_CARLOS), login(RRHH), login(ADMIN)}) {
            mvc.perform(put("/api/v1/vacantes/1/ranking").header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().is4xxClientError());
            mvc.perform(patch("/api/v1/vacantes/1/ranking/" + nicolas).header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"posicion\":1}"))
                    .andExpect(status().is4xxClientError());
        }
        // El orden no cambio
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(jsonPath("$.candidatos[0].postulacionId").value(santiago));
    }

    @Test
    void elManagerRecibe409AlIntentarReordenar() throws Exception {
        mvc.perform(post("/api/v1/vacantes/1/ranking").header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("inmutable")));
    }

    @Test
    void escenario2_desgloseConCostoDeRotacion() throws Exception {
        int antes = jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class);
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/desglose").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidato").value("Empleado_008"))
                .andExpect(jsonPath("$.desglose.porcentajeHabilidades").exists())
                .andExpect(jsonPath("$.costoRotacion.costoSiRenuncia").value(11000))
                .andExpect(jsonPath("$.costoRotacion.costoSiRota").value(2000))
                .andExpect(jsonPath("$.costoRotacion.ahorro").value(9000));
        assertEquals(antes + 1, jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class));
    }

    @Test
    void conVeinteCandidatosElRankingCargaEnMenosDe2Segundos() throws Exception {
        for (int i = 0; i < 20; i++) {
            Long id = jdbc.queryForObject("""
                    INSERT INTO usuario (rol, nombre, correo, contrasena_hash) VALUES ('EMPLEADO', 'Carga', ?, 'x')
                    RETURNING id""", Long.class, "carga" + i + "@talentmatch.com");
            jdbc.update("INSERT INTO empleado (id, senioridad) VALUES (?, 'SENIOR')", id);
            postular(id, 1, 65 + i);
        }
        String token = login(MANAGER_CARLOS);
        long inicio = System.nanoTime();
        mvc.perform(get("/api/v1/vacantes/1/ranking").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatos.length()").value(24));
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(ms < 2000, "El ranking tardo " + ms + " ms");
    }

    @Test
    void aceptarCierraLaVacanteMueveAlEmpleadoYNotificaSoloARrhh() throws Exception {
        // Santiago tambien estaba postulado a Data Engineer (vacante 3, manager Andres)
        long otra = postular(11, 3, 40);

        mvc.perform(post("/api/v1/postulaciones/" + santiago + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        assertEquals("CERRADA", jdbc.queryForObject("SELECT estado FROM vacante WHERE id = 1", String.class));
        // Los demas candidatos quedan no seleccionados, con su posicion registrada
        assertEquals("RECHAZADA", jdbc.queryForObject("SELECT estado FROM postulacion WHERE id = ?", String.class, sofia));
        assertEquals(2, jdbc.queryForObject("SELECT posicion_ranking FROM postulacion WHERE id = ?", Integer.class, sofia));
        assertEquals(5, jdbc.queryForObject("SELECT total_candidatos FROM postulacion WHERE id = ?", Integer.class, sofia));
        // RN-09: su otra postulacion se cancela, sin notificar al manager de esa vacante
        assertEquals("CANCELADA_POR_ROTACION",
                jdbc.queryForObject("SELECT estado FROM postulacion WHERE id = ?", String.class, otra));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM notificacion WHERE destinatario_id = 6", Integer.class));
        // RN-15: nueva asignacion en el proyecto de la vacante; su puesto anterior genera una vacante diferida
        assertEquals(1, jdbc.queryForObject("""
                SELECT proyecto_id FROM asignacion_proyecto WHERE empleado_id = 11 AND fecha_fin IS NULL""", Integer.class));
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM vacante WHERE titulo = 'Cloud Engineer' AND estado = 'PENDIENTE_ACTIVACION'""", Integer.class));
        assertEquals(4, jdbc.queryForObject("SELECT manager_id FROM empleado WHERE id = 11", Integer.class));
        // RN-16: RR.HH. recibe la notificacion
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacion WHERE destinatario_id = 2 AND tipo = 'ROTACION_ACEPTADA'", Integer.class));
    }

    @Test
    void rechazarNoPideJustificacion() throws Exception {
        mvc.perform(post("/api/v1/postulaciones/" + nicolas + "/rechazar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
        assertEquals("ACTIVA", jdbc.queryForObject("SELECT estado FROM vacante WHERE id = 1", String.class));
    }

    @Test
    void noSeDecideSobreCandidatosFueraDelRankingNiDeOtroManager() throws Exception {
        mvc.perform(post("/api/v1/postulaciones/" + camila + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/postulaciones/" + sofia + "/aceptar").header("Authorization", login(MANAGER_DIANA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasPeticionesNuevasEsperanAlSiguienteCiclo() throws Exception {
        jdbc.update("UPDATE postulacion SET fecha_creacion = CURRENT_TIMESTAMP WHERE id = ?", sofia);
        mvc.perform(post("/api/v1/postulaciones/" + sofia + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("próximo ciclo")));
    }
}
