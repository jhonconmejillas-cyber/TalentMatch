package co.edu.javeriana.talentmatch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de las pruebas de integracion de la API: arranca la aplicacion completa
 * (con PostgreSQL y los datos de prueba) y permite iniciar sesion con los usuarios seed.
 * {@code @Transactional} deshace los cambios de cada prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTest {

    /** Contrasena de todos los usuarios de database/seeds. */
    protected static final String CONTRASENA_SEED = "TalentMatch2026";

    protected static final String ADMIN = "admin@talentmatch.com";
    protected static final String RRHH = "rrhh@talentmatch.com";
    protected static final String LIDER = "lider@talentmatch.com";
    /** Manager de Backend (id 4): equipo de Juan, Sofia, Mateo, Samuel y Mariana. */
    protected static final String MANAGER_CARLOS = "carlos.perez@talentmatch.com";
    /** Manager de Cloud (id 5). */
    protected static final String MANAGER_DIANA = "diana.torres@talentmatch.com";
    /** Empleado id 7, Senior Backend, 4 meses en el rol. */
    protected static final String EMPLEADO_JUAN = "juan.ramirez@talentmatch.com";
    /** Empleado id 8, Senior Backend, 14 meses en el rol. */
    protected static final String EMPLEADO_SOFIA = "sofia.castro@talentmatch.com";

    @Autowired
    protected MockMvc mvc;

    /** Inicia sesion y devuelve el encabezado "Bearer <token>". */
    protected String login(String correo) throws Exception {
        String json = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + CONTRASENA_SEED + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(json, "$.token");
    }
}
