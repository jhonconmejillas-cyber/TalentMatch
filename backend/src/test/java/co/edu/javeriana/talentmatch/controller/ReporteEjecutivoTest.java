package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-010: reporte de los 10 empleados mas destacados. */
class ReporteEjecutivoTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void escenario1_top10OrdenadoConLosTresCriterios() throws Exception {
        mvc.perform(get("/api/v1/reportes/top10").header("Authorization", login(LIDER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empleados.length()").value(10))
                // Sofia: 4 avaladas y 1 rotacion -> primera entre los empleados con 4 avaladas
                .andExpect(jsonPath("$.empleados[0].nombre").value("Sofia Castro"))
                .andExpect(jsonPath("$.empleados[0].habilidadesAvaladas").value(4))
                .andExpect(jsonPath("$.empleados[0].rotaciones").value(1))
                .andExpect(jsonPath("$.empleados[0].aniosEnLaEmpresa").exists())
                // Empate en avaladas y rotaciones: gana quien lleva mas anios (Nicolas 9 > Santiago 7)
                .andExpect(jsonPath("$.empleados[1].nombre").value("Nicolas Rios"))
                .andExpect(jsonPath("$.empleados[2].nombre").value("Santiago Mora"));
    }

    @Test
    void soloCuentanLasHabilidadesAvaladas() throws Exception {
        // Juan tiene 3 avaladas y Kubernetes agregado: le agregamos 5 mas sin avalar y no sube
        for (long h : new long[] {7, 8, 9, 10, 11}) {
            jdbc.update("INSERT INTO habilidad_empleado (empleado_id, habilidad_id) VALUES (7, ?)", h);
        }
        mvc.perform(get("/api/v1/reportes/top10").header("Authorization", login(LIDER)))
                .andExpect(jsonPath("$.empleados[?(@.nombre == 'Juan Ramirez')].habilidadesAvaladas").value(3));
    }

    @Test
    void escenario3_laPantallaIndicaQueEsSoloUnInsumo() throws Exception {
        mvc.perform(get("/api/v1/reportes/top10").header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aviso", containsString("insumo de apoyo")))
                .andExpect(jsonPath("$.aviso", containsString("no ejecuta ni registra decisiones de ascenso")));
    }

    @Test
    void soloLiderEjecutivoYRrhhLoVen() throws Exception {
        for (String correo : new String[] {EMPLEADO_JUAN, MANAGER_CARLOS, ADMIN}) {
            mvc.perform(get("/api/v1/reportes/top10").header("Authorization", login(correo)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void escenario2_laRotacionConfirmadaInformaElIncentivo() throws Exception {
        jdbc.update("UPDATE vacante SET fecha_activacion = CURRENT_TIMESTAMP - INTERVAL '10 days' WHERE id = 1");
        Long id = jdbc.queryForObject("""
                INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final, fecha_creacion)
                VALUES (8, 1, 86, 86, CURRENT_TIMESTAMP - INTERVAL '2 days') RETURNING id""", Long.class);
        mvc.perform(post("/api/v1/postulaciones/" + id + "/aceptar").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/empleados/me/postulaciones").header("Authorization", login(EMPLEADO_SOFIA)))
                .andExpect(jsonPath("$[0].estado").value("ACEPTADA"))
                .andExpect(jsonPath("$[0].mensaje", containsString("aumentan tu probabilidad")));
    }
}
