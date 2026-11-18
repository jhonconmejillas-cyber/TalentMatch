package co.edu.javeriana.talentmatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PerfilControllerTest extends ApiTest {

    @Test
    void elEmpleadoConsultaSuPerfil() throws Exception {
        mvc.perform(get("/api/v1/empleados/me/perfil").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senioridad").value("SENIOR"))
                .andExpect(jsonPath("$.proyectoActual.nombre").value("Pasarela de Pagos"))
                .andExpect(jsonPath("$.manager").value("Carlos Perez"));
    }

    @Test
    void elEmpleadoEditaSuPerfilYLosCambiosPersisten() throws Exception {
        String token = login(EMPLEADO_JUAN);
        mvc.perform(put("/api/v1/empleados/me/perfil").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senioridad\":\"LIDER\",\"aniosExperiencia\":9,\"area\":\"Plataforma\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/empleados/me/perfil").header("Authorization", token))
                .andExpect(jsonPath("$.senioridad").value("LIDER"))
                .andExpect(jsonPath("$.aniosExperiencia").value(9))
                .andExpect(jsonPath("$.area").value("Plataforma"));
    }

    @Test
    void losDatosInvalidosIndicanElCampo() throws Exception {
        mvc.perform(put("/api/v1/empleados/me/perfil").header("Authorization", login(EMPLEADO_JUAN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senioridad\":\"SENIOR\",\"aniosExperiencia\":-1,\"area\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aniosExperiencia").exists())
                .andExpect(jsonPath("$.campos.area").exists());
    }

    @Test
    void rrhhNoTienePerfilDeEmpleado() throws Exception {
        mvc.perform(get("/api/v1/empleados/me/perfil").header("Authorization", login(RRHH)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listaLosProyectosActivos() throws Exception {
        mvc.perform(get("/api/v1/proyectos").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }
}
