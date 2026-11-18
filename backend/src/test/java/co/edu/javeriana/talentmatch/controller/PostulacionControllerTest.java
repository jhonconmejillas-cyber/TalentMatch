package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;

class PostulacionControllerTest extends ApiTest {

    @Test
    void juanLleva4MesesYVeElBotonDeshabilitadoConLaFecha() throws Exception {
        String juan = login(EMPLEADO_JUAN);
        mvc.perform(get("/api/v1/vacantes/2/elegibilidad").header("Authorization", juan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedePostularse").value(false))
                .andExpect(jsonPath("$.fechaHabilitacion").exists())
                .andExpect(jsonPath("$.motivo", startsWith("Podrás postularte a partir del")));

        mvc.perform(post("/api/v1/vacantes/2/postulaciones").header("Authorization", juan))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("6 meses en el rol actual")));
    }

    @Test
    void sofiaSePostulaAVariasVacantesAunqueTengaPuntajeBajo() throws Exception {
        String sofia = login(EMPLEADO_SOFIA);
        mvc.perform(post("/api/v1/vacantes/1/postulaciones").header("Authorization", sofia))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.viable").value(true));
        mvc.perform(post("/api/v1/vacantes/3/postulaciones").header("Authorization", sofia))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postulacion.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.viable").value(false))
                .andExpect(jsonPath("$.aviso", containsString("posibilidades son bajas")));

        mvc.perform(get("/api/v1/empleados/me/postulaciones").header("Authorization", sofia))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void noSePostulaDosVecesALaMismaVacante() throws Exception {
        String sofia = login(EMPLEADO_SOFIA);
        mvc.perform(post("/api/v1/vacantes/1/postulaciones").header("Authorization", sofia))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/vacantes/1/postulaciones").header("Authorization", sofia))
                .andExpect(status().isConflict());
    }

    @Test
    void rrhhNoSePostula() throws Exception {
        mvc.perform(post("/api/v1/vacantes/1/postulaciones").header("Authorization", login(RRHH)))
                .andExpect(status().isForbidden());
    }
}
