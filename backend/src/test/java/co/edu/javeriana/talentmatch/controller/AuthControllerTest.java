package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class AuthControllerTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;

    private String registro(String nombre, String correo, String contrasena) {
        return "{\"nombre\":\"" + nombre + "\",\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    @Test
    void unUsuarioSeRegistraIniciaSesionYVeSuRol() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Laura Nueva", "laura.nueva@talentmatch.com", "Segura123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("EMPLEADO"))
                .andExpect(jsonPath("$", not(hasKey("contrasenaHash"))));

        String json = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"laura.nueva@talentmatch.com\",\"contrasena\":\"Segura123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("EMPLEADO"))
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(json, "$.token");

        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("laura.nueva@talentmatch.com"));
    }

    @Test
    void laContrasenaSeGuardaConHashBcrypt() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Hash", "hash.prueba@talentmatch.com", "Segura123")))
                .andExpect(status().isCreated());

        String guardada = jdbc.queryForObject(
                "SELECT contrasena_hash FROM usuario WHERE correo = 'hash.prueba@talentmatch.com'", String.class);
        assertTrue(guardada.startsWith("$2"), "Debe ser un hash BCrypt");
        assertTrue(!guardada.contains("Segura123"));
    }

    @Test
    void elRegistroExigeCorreoCorporativo() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Externo", "externo@gmail.com", "Segura123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.correo", startsWith("Usa tu correo corporativo")));
    }

    @Test
    void elRegistroValidaLosCampos() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("", "no-es-correo", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.correo").exists());

        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Corta", "corta@talentmatch.com", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.contrasena").exists());
    }

    @Test
    void noSePuedeRegistrarDosVecesElMismoCorreo() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Juan", EMPLEADO_JUAN, "Segura123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.correo").exists());
    }

    @Test
    void unaContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + EMPLEADO_JUAN + "\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    void sinTokenNoSeAccedeALasRutasProtegidas() throws Exception {
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elHealthEsPublico() throws Exception {
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"));
    }

    @Test
    void unRolSinPermisoRecibe403() throws Exception {
        String empleado = login(EMPLEADO_JUAN);
        mvc.perform(get("/api/v1/usuarios").header("Authorization", empleado))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para realizar esta acción."));

        String manager = login(MANAGER_CARLOS);
        mvc.perform(get("/api/v1/usuarios").header("Authorization", manager)).andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorListaUsuariosYCambiaRoles() throws Exception {
        String admin = login(ADMIN);
        mvc.perform(get("/api/v1/usuarios").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16));

        // Sofia (8) pasa de EMPLEADO a MANAGER
        mvc.perform(patch("/api/v1/usuarios/8").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rol\":\"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("MANAGER"));
    }

    @Test
    void unaCuentaDesactivadaNoPuedeIniciarSesion() throws Exception {
        String admin = login(ADMIN);
        mvc.perform(patch("/api/v1/usuarios/9").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"mateo.vargas@talentmatch.com\",\"contrasena\":\"" + CONTRASENA_SEED + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdministradorNoPuedeDesactivarseASiMismo() throws Exception {
        String admin = login(ADMIN);
        mvc.perform(patch("/api/v1/usuarios/1").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isBadRequest());
    }
}
