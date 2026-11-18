package co.edu.javeriana.talentmatch.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import co.edu.javeriana.talentmatch.config.SeguridadProperties;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/** HU-006: proteccion de los datos sensibles de los empleados. */
@TestPropertySource(properties = "JWT_SECRET=clave-de-pruebas-de-al-menos-32-caracteres!!")
class ProteccionDatosTest extends ApiTest {

    private static final String SECRETO = "clave-de-pruebas-de-al-menos-32-caracteres!!";

    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired jakarta.persistence.EntityManager em;

    private int accesosDenegados() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM bitacora_acceso", Integer.class);
    }

    @Test
    void escenario1_unManagerSinRelacionNoVeElHistorialYQuedaRegistrado() throws Exception {
        int antes = accesosDenegados();
        // Diana (Cloud) no es manager de Juan (Backend) ni comparten vacante
        mvc.perform(get("/api/v1/empleados/7/historial").header("Authorization", login(MANAGER_DIANA)))
                .andExpect(status().isForbidden());

        assertEquals(antes + 1, accesosDenegados());
        assertEquals("/api/v1/empleados/7/historial", jdbc.queryForObject(
                "SELECT recurso FROM bitacora_acceso ORDER BY id DESC LIMIT 1", String.class).substring(4));
        assertEquals(5L, jdbc.queryForObject("SELECT usuario_id FROM bitacora_acceso ORDER BY id DESC LIMIT 1", Long.class));
    }

    @Test
    void elManagerActualVeProyectosYRotacionesPeroNoLosRechazos() throws Exception {
        mvc.perform(get("/api/v1/empleados/8/historial").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rotaciones").value(1))
                .andExpect(jsonPath("$.proyectos.length()").value(2))
                .andExpect(jsonPath("$.postulaciones").doesNotExist());
    }

    @Test
    void unaVacanteEnComunDaRelacionAlManager() throws Exception {
        jdbc.update("INSERT INTO postulacion (empleado_id, vacante_id) VALUES (7, 2)"); // Juan -> vacante de Diana
        mvc.perform(get("/api/v1/empleados/7/historial").header("Authorization", login(MANAGER_DIANA)))
                .andExpect(status().isOk());
    }

    @Test
    void rrhhVeElHistorialCompleto() throws Exception {
        jdbc.update("INSERT INTO postulacion (empleado_id, vacante_id) VALUES (7, 2)");
        mvc.perform(get("/api/v1/empleados/7/historial").header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulaciones.length()").value(1));
    }

    @Test
    void unRolSinPermisoTambienQuedaEnLaBitacora() throws Exception {
        int antes = accesosDenegados();
        mvc.perform(get("/api/v1/alertas").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isForbidden());
        assertEquals(antes + 1, accesosDenegados());
        mvc.perform(get("/api/v1/gestion/accesos-denegados").header("Authorization", login(RRHH)))
                .andExpect(jsonPath("$[0].recurso").value("GET /api/v1/alertas"));
    }

    @Test
    void escenario2_ningunaRespuestaExponeElHashDeLaContrasena() throws Exception {
        for (String ruta : List.of("/api/v1/me", "/api/v1/usuarios", "/api/v1/empleados/me/perfil")) {
            String correo = ruta.contains("usuarios") ? ADMIN : EMPLEADO_JUAN;
            String json = mvc.perform(get(ruta).header("Authorization", login(correo)))
                    .andReturn().getResponse().getContentAsString();
            assertTrue(!json.contains("$2a$") && !json.toLowerCase().contains("contrasena"), ruta + ": " + json);
        }
    }

    @Test
    void escenario3_cadaPeticionRenuevaElTokenDeInactividad() throws Exception {
        mvc.perform(get("/api/v1/me").header("Authorization", login(EMPLEADO_JUAN)))
                .andExpect(status().isOk())
                .andExpect(header().exists(RenovacionTokenFilter.ENCABEZADO));
    }

    @Test
    void escenario3_trasMasDe30MinutosSinActividadLaSesionSeCierra() throws Exception {
        Usuario juan = usuarios.findByCorreoIgnoreCase(EMPLEADO_JUAN).orElseThrow();
        // Token emitido hace 31 minutos con la misma clave de firma
        Clock hace31 = Clock.offset(Clock.systemDefaultZone(), Duration.ofMinutes(-31));
        TokenService pasado = new TokenService(SECRETO, new SeguridadProperties(30, 8, List.of("talentmatch.com")), hace31);
        String vencido = pasado.emitir(juan).token();

        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized());

        // Con 29 minutos de inactividad la sesion sigue viva
        Clock hace29 = Clock.offset(Clock.systemDefaultZone(), Duration.ofMinutes(-29));
        String vigente = new TokenService(SECRETO, new SeguridadProperties(30, 8, List.of("talentmatch.com")), hace29)
                .emitir(juan).token();
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + vigente)).andExpect(status().isOk());
    }

    @Test
    void unaCuentaDesactivadaPierdeLaSesionDeInmediato() throws Exception {
        String token = login(EMPLEADO_JUAN);
        jdbc.update("UPDATE usuario SET activo = FALSE WHERE id = 7");
        em.clear(); // la prueba comparte la transaccion: se descarta la copia en memoria de Juan
        mvc.perform(get("/api/v1/me").header("Authorization", token)).andExpect(status().isUnauthorized());
    }
}
