package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-007: avalar las habilidades del equipo. */
class HabilidadControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    private Long idHabilidadKubernetesDeJuan() {
        return jdbc.queryForObject("SELECT id FROM habilidad_empleado WHERE empleado_id = 7 AND habilidad_id = 6", Long.class);
    }

    @Test
    void unaHabilidadNuevaNaceAgregada() throws Exception {
        mvc.perform(post("/api/v1/empleados/me/habilidades").header("Authorization", login(EMPLEADO_JUAN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"habilidadId\":7,\"nivelDeclarado\":\"Basico\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habilidad").value("Go"))
                .andExpect(jsonPath("$.estado").value("AGREGADO"));
    }

    @Test
    void noSeAgregaDosVecesLaMismaHabilidad() throws Exception {
        mvc.perform(post("/api/v1/empleados/me/habilidades").header("Authorization", login(EMPLEADO_JUAN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"habilidadId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.habilidadId").exists());
    }

    @Test
    void escenario1_laBandejaMuestraLasPendientesDelEquipo() throws Exception {
        mvc.perform(get("/api/v1/equipo/habilidades?estado=AGREGADO").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("AGREGADO"))))
                .andExpect(jsonPath("$[*].empleado", hasItem("Juan Ramirez")))
                // Santiago es del equipo de Diana, no de Carlos
                .andExpect(jsonPath("$[*].empleado", not(hasItem("Santiago Mora"))))
                .andExpect(jsonPath("$[0].nivelDeclarado").exists())
                .andExpect(jsonPath("$[0].fechaAgregado").exists());
    }

    @Test
    void escenario2_elManagerActualAvala() throws Exception {
        mvc.perform(patch("/api/v1/habilidades-empleado/" + idHabilidadKubernetesDeJuan())
                        .header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"AVALADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("AVALADO"))
                .andExpect(jsonPath("$.fechaAval").exists());
    }

    @Test
    void escenario2_elManagerRechazaYLaHabilidadSigueVisible() throws Exception {
        mvc.perform(patch("/api/v1/habilidades-empleado/" + idHabilidadKubernetesDeJuan())
                        .header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"RECHAZADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"));

        mvc.perform(get("/api/v1/empleados/me/habilidades").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(jsonPath("$[*].habilidad", hasItem("Kubernetes")));
    }

    @Test
    void otroManagerNoPuedeAvalar() throws Exception {
        mvc.perform(patch("/api/v1/habilidades-empleado/" + idHabilidadKubernetesDeJuan())
                        .header("Authorization", login(MANAGER_DIANA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"AVALADO\"}"))
                .andExpect(status().isForbidden());
        Object estado = jdbc.queryForObject("SELECT estado FROM habilidad_empleado WHERE id = ?", String.class,
                idHabilidadKubernetesDeJuan());
        org.junit.jupiter.api.Assertions.assertEquals("AGREGADO", estado);
    }

    @Test
    void unEmpleadoNoPuedeAvalar() throws Exception {
        mvc.perform(patch("/api/v1/habilidades-empleado/" + idHabilidadKubernetesDeJuan())
                        .header("Authorization", login(EMPLEADO_SOFIA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"AVALADO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSeAvalaDosVeces() throws Exception {
        Long idJava = jdbc.queryForObject("SELECT id FROM habilidad_empleado WHERE empleado_id = 7 AND habilidad_id = 1", Long.class);
        mvc.perform(patch("/api/v1/habilidades-empleado/" + idJava).header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"AVALADO\"}"))
                .andExpect(status().isConflict());
    }
}
