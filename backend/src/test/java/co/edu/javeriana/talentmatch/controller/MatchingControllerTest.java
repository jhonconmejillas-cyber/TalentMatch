package co.edu.javeriana.talentmatch.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class MatchingControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void devuelveElPuntajeConDesgloseYLoRegistraEnLaBitacora() throws Exception {
        int antes = jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class);

        // Sofia (Senior): Java, Spring Boot, PostgreSQL y Docker avaladas; vacante 1 = Backend Senior
        mvc.perform(get("/api/v1/vacantes/1/mi-puntaje").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntajeFinal").value(86.0)) // 4/5 x 0.7 + 1 x 0.3
                .andExpect(jsonPath("$.viable").value(true))
                .andExpect(jsonPath("$.desglose.habilidadesFaltantes[0]").value("Kafka"))
                .andExpect(jsonPath("$.desglose.habilidadesSinAvalar[0]").value("Kafka"));

        assertEquals(antes + 1, jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_calculo", Integer.class));
    }

    @Test
    void avisaPosibilidadesBajas() throws Exception {
        // Juan (Senior Backend) frente a Data Engineer
        mvc.perform(get("/api/v1/vacantes/3/mi-puntaje").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viable").value(false))
                .andExpect(jsonPath("$.aviso", Matchers.containsString("posibilidades son bajas")));
    }

    @Test
    void laRespuestaNoIncluyeDatosPersonales() throws Exception {
        mvc.perform(get("/api/v1/vacantes/1/mi-puntaje").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(jsonPath("$.nombre").doesNotExist())
                .andExpect(jsonPath("$.correo").doesNotExist());
    }

    @Test
    void rrhhNoTienePuntajePropio() throws Exception {
        mvc.perform(get("/api/v1/vacantes/1/mi-puntaje").header("Authorization", login(RRHH)))
                .andExpect(status().isForbidden());
    }
}
