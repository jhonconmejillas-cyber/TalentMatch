package co.edu.javeriana.talentmatch.controller;

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

class GestionControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    private Long asignacionDeSofia() {
        return jdbc.queryForObject("SELECT id FROM asignacion_proyecto WHERE empleado_id = 8 AND fecha_fin IS NULL", Long.class);
    }

    @Test
    void rrhhRegistraUnaRenunciaYLaVacanteQuedaActiva() throws Exception {
        String rrhh = login(RRHH);
        mvc.perform(get("/api/v1/asignaciones").header("Authorization", rrhh))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/asignaciones/" + asignacionDeSofia() + "/liberar").header("Authorization", rrhh)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"RENUNCIA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.origen").value("RENUNCIA"));
    }

    @Test
    void laRotacionAceptadaNoSeRegistraAMano() throws Exception {
        mvc.perform(post("/api/v1/asignaciones/" + asignacionDeSofia() + "/liberar").header("Authorization", login(RRHH))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"ROTACION_ACEPTADA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloRrhhGestionaAsignaciones() throws Exception {
        mvc.perform(post("/api/v1/asignaciones/" + asignacionDeSofia() + "/liberar").header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"RENUNCIA\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/gestion/vacantes").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rrhhVeLasVacantesPorEstado() throws Exception {
        mvc.perform(get("/api/v1/gestion/vacantes?estado=ACTIVA").header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void cadaUsuarioVeYMarcaSoloSusNotificaciones() throws Exception {
        Long id = jdbc.queryForObject("""
                INSERT INTO notificacion (destinatario_id, tipo, titulo, mensaje) VALUES (7, 'ALERTA', 't', 'm') RETURNING id""",
                Long.class);
        mvc.perform(get("/api/v1/notificaciones").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(jsonPath("$[0].titulo").value("t"));
        mvc.perform(patch("/api/v1/notificaciones/" + id).header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/notificaciones/" + id).header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }
}
