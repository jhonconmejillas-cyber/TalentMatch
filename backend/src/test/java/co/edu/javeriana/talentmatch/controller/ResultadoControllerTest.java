package co.edu.javeriana.talentmatch.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-003: resultado anonimo del rechazo. */
class ResultadoControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    long seleccionada;
    long sofia;
    long isabella;
    long nicolas;

    private long postular(long empleado, double puntaje) {
        return jdbc.queryForObject("""
                INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final, fecha_creacion)
                VALUES (?, 1, ?, ?, CURRENT_TIMESTAMP - INTERVAL '2 days') RETURNING id""",
                Long.class, empleado, puntaje, puntaje);
    }

    @BeforeEach
    void preparar() throws Exception {
        jdbc.update("UPDATE vacante SET fecha_activacion = CURRENT_TIMESTAMP - INTERVAL '10 days' WHERE id = 1");
        seleccionada = postular(11, 84);  // Santiago: Docker, Kubernetes, Terraform, AWS avaladas
        sofia = postular(8, 80);           // Java, Spring Boot, PostgreSQL, Docker
        isabella = postular(12, 70);       // CI/CD, Linux, Docker
        nicolas = postular(13, 68);        // Python, SQL, Spark, Kafka
        mvc.perform(post("/api/v1/postulaciones/" + seleccionada + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk());
    }

    @Test
    void escenario1_laNotificacionLlegaSolaAlCerrarseLaVacante() {
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*) FROM notificacion WHERE destinatario_id = 8 AND tipo = 'RESULTADO_POSTULACION'
                AND postulacion_id = ?""", Integer.class, sofia));
        assertEquals("CERRADA", jdbc.queryForObject("SELECT estado FROM vacante WHERE id = 1", String.class));
    }

    @Test
    void escenario2_comparacionConPuntajesYTablaDeHabilidades() throws Exception {
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/resultado").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miPuntajeFinal").value(80.0))
                .andExpect(jsonPath("$.puntajeSeleccionado").value(84.0))
                .andExpect(jsonPath("$.posicion").value(2))
                .andExpect(jsonPath("$.comparacion[?(@.habilidad == 'Kubernetes')].laTieneElSeleccionado").value(true))
                .andExpect(jsonPath("$.comparacion[?(@.habilidad == 'Kubernetes')].laTengoAvalada").value(false))
                .andExpect(jsonPath("$.comparacion[?(@.habilidad == 'Java')].laTengoAvalada").value(true))
                .andExpect(jsonPath("$.repostulacionDesde").exists());
    }

    @Test
    void ningunaRespuestaIncluyeDatosDelSeleccionado() throws Exception {
        for (long[] caso : new long[][] {{sofia, 8}, {isabella, 12}, {nicolas, 13}}) {
            String correo = jdbc.queryForObject("SELECT correo FROM usuario WHERE id = ?", String.class, caso[1]);
            String json = mvc.perform(get("/api/v1/postulaciones/" + caso[0] + "/resultado")
                            .header("Authorization", login(correo)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            for (String prohibido : List.of("Santiago", "Mora", "santiago.mora", "Empleado_011", "Cloud",
                    "\"empleadoId\"", "\"nombre\"", "\"correo\"", "\"area\"", ":" + seleccionada + ",")) {
                assertFalse(json.contains(prohibido), "La respuesta expone '" + prohibido + "': " + json);
            }
        }
    }

    @Test
    void tresCombinacionesDistintasDeBrechas() throws Exception {
        Map<String, Boolean> sofiaTiene = tengo(sofia, EMPLEADO_SOFIA);
        Map<String, Boolean> isabellaTiene = tengo(isabella, "isabella.herrera@talentmatch.com");
        Map<String, Boolean> nicolasTiene = tengo(nicolas, "nicolas.rios@talentmatch.com");

        // Ninguno tiene Kubernetes, que si tiene el seleccionado
        assertFalse(sofiaTiene.get("Kubernetes") || isabellaTiene.get("Kubernetes") || nicolasTiene.get("Kubernetes"));
        // Docker: Sofia e Isabella lo tienen; Nicolas no
        assertTrue(sofiaTiene.get("Docker") && isabellaTiene.get("Docker"));
        assertFalse(nicolasTiene.get("Docker"));
        // Java solo lo tiene Sofia
        assertTrue(sofiaTiene.get("Java"));
        assertFalse(isabellaTiene.get("Java") || nicolasTiene.get("Java"));
    }

    private Map<String, Boolean> tengo(long postulacion, String correo) throws Exception {
        String json = mvc.perform(get("/api/v1/postulaciones/" + postulacion + "/resultado")
                .header("Authorization", login(correo))).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> filas = JsonPath.read(json, "$.comparacion");
        return filas.stream().collect(java.util.stream.Collectors.toMap(
                f -> (String) f.get("habilidad"), f -> (Boolean) f.get("laTengoAvalada")));
    }

    @Test
    void soloElEmpleadoYRrhhVenElResultado() throws Exception {
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/resultado").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/resultado").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/resultado").header("Authorization", login(RRHH)))
                .andExpect(status().isOk());
    }

    @Test
    void escenario3_agregarLaBrechaALaWishlist() throws Exception {
        String token = login(EMPLEADO_SOFIA);
        mvc.perform(post("/api/v1/empleados/me/wishlist/6").header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[*].nombre").value(org.hamcrest.Matchers.hasItem("Kubernetes")));
        mvc.perform(get("/api/v1/postulaciones/" + sofia + "/resultado").header("Authorization", token))
                .andExpect(jsonPath("$.comparacion[?(@.habilidad == 'Kubernetes')].enMiWishlist").value(true))
                .andExpect(jsonPath("$.queSigue", org.hamcrest.Matchers.containsString("nueva rotación")));
        mvc.perform(delete("/api/v1/empleados/me/wishlist/6").header("Authorization", token))
                .andExpect(status().isOk());
    }
}
