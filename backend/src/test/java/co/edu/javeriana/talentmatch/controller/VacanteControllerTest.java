package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class VacanteControllerTest extends ApiTest {

    private static final String NUEVA = """
            {"titulo":"QA Automation","descripcion":"Pruebas","senioridad":"SEMI_SENIOR","area":"Calidad",
             "proyectoId":1,"habilidadesCriticasIds":[1,14],"habilidadesDeseablesIds":[14,19]}""";

    @Test
    void soloUnManagerPuedePublicar() throws Exception {
        mvc.perform(post("/api/v1/vacantes").header("Authorization", login(EMPLEADO_JUAN))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/vacantes").header("Authorization", login(RRHH))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isForbidden());
    }

    @Test
    void elManagerPublicaUnaVacanteActiva() throws Exception {
        mvc.perform(post("/api/v1/vacantes").header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.manager").value("Carlos Perez"))
                .andExpect(jsonPath("$.habilidadesCriticas.length()").value(2))
                // Git era critica y deseable: queda solo como critica
                .andExpect(jsonPath("$.habilidadesDeseables[*].nombre").value(hasItem("CI/CD")))
                .andExpect(jsonPath("$.habilidadesDeseables.length()").value(1));
    }

    @Test
    void publicarValidaLosCampos() throws Exception {
        mvc.perform(post("/api/v1/vacantes").header("Authorization", login(MANAGER_CARLOS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"area\":\"\",\"habilidadesCriticasIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.senioridad").exists())
                .andExpect(jsonPath("$.campos.habilidadesCriticasIds").exists());
    }

    @Test
    void unEmpleadoVeVacantesDeAreasDistintasALaSuya() throws Exception {
        // Juan es de Backend y ve Cloud, Datos y Frontend (RN-11)
        mvc.perform(get("/api/v1/vacantes").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].area", hasItems("Backend", "Cloud", "Datos", "Frontend")));
    }

    @Test
    void filtraPorTituloAreaSenioridadYHabilidad() throws Exception {
        String token = login(EMPLEADO_JUAN);
        mvc.perform(get("/api/v1/vacantes?titulo=engineer").header("Authorization", token))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/vacantes?area=cloud").header("Authorization", token))
                .andExpect(jsonPath("$[*].titulo", everyItem(is("Cloud Engineer"))));
        mvc.perform(get("/api/v1/vacantes?senioridad=SENIOR").header("Authorization", token))
                .andExpect(jsonPath("$[*].senioridad", everyItem(is("SENIOR"))));
        // Kafka (16) es deseable en Backend Senior y en Data Engineer
        mvc.perform(get("/api/v1/vacantes?habilidadId=16").header("Authorization", token))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void lasVacantesNoActivasNoAparecenEnLaBusqueda() throws Exception {
        mvc.perform(get("/api/v1/vacantes").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(jsonPath("$[*].estado", everyItem(is("ACTIVA"))));
    }

    @Test
    void elManagerVeSusVacantes() throws Exception {
        mvc.perform(get("/api/v1/equipo/vacantes").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].manager", everyItem(is("Carlos Perez"))));
    }
}
