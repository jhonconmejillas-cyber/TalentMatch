package co.edu.javeriana.talentmatch.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import co.edu.javeriana.talentmatch.ApiTest;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Recorre la matriz de permisos de docs/seguridad/Matriz-de-Permisos.md contra la API:
 * "SI" = el rol puede entrar (no recibe 401 ni 403), "NO" = recibe 403.
 */
class MatrizPermisosTest extends ApiTest {

    private static final Map<String, String> CORREO = Map.of(
            "EMPLEADO", EMPLEADO_SOFIA,
            "MANAGER", MANAGER_CARLOS,
            "RRHH", RRHH,
            "LIDER", LIDER,
            "ADMIN", ADMIN);

    @ParameterizedTest(name = "{0} {1} -> EMP={2} MGR={3} RRHH={4} LIDER={5} ADMIN={6}")
    @CsvSource({
        "GET,   /api/v1/usuarios,                 NO, NO, NO, NO, SI",
        "GET,   /api/v1/empleados/me/perfil,      SI, SI, NO, NO, NO",
        "GET,   /api/v1/empleados/me/habilidades, SI, SI, NO, NO, NO",
        "GET,   /api/v1/empleados/me/wishlist,    SI, SI, NO, NO, NO",
        "GET,   /api/v1/equipo/habilidades,       NO, SI, NO, NO, NO",
        "GET,   /api/v1/vacantes,                 SI, SI, SI, NO, NO",
        "GET,   /api/v1/equipo/vacantes,          NO, SI, NO, NO, NO",
        "GET,   /api/v1/vacantes/1/mi-puntaje,    SI, SI, NO, NO, NO",
        "GET,   /api/v1/empleados/me/postulaciones, SI, SI, NO, NO, NO",
        "GET,   /api/v1/vacantes/1/ranking,       NO, SI, SI, NO, NO",
        "GET,   /api/v1/empleados/7/historial,    NO, SI, SI, NO, NO",
        "GET,   /api/v1/alertas,                  NO, NO, SI, NO, NO",
        "GET,   /api/v1/asignaciones,             NO, NO, SI, NO, NO",
        "GET,   /api/v1/gestion/vacantes,         NO, NO, SI, NO, NO",
        "GET,   /api/v1/gestion/accesos-denegados, NO, NO, SI, NO, NO",
        "GET,   /api/v1/reportes/rechazos,        NO, NO, SI, NO, NO",
        "GET,   /api/v1/reportes/top10,           NO, NO, SI, SI, NO",
        "GET,   /api/v1/me,                       SI, SI, SI, SI, SI",
        "GET,   /api/v1/notificaciones,           SI, SI, SI, SI, SI",
    })
    void cadaEndpointVerificaElRol(String metodo, String ruta, String emp, String mgr, String rrhh, String lider,
                                   String admin) throws Exception {
        Map<String, String> esperado = Map.of("EMPLEADO", emp, "MANAGER", mgr, "RRHH", rrhh, "LIDER", lider,
                "ADMIN", admin);
        for (Map.Entry<String, String> e : esperado.entrySet()) {
            int estado = mvc.perform(request(HttpMethod.valueOf(metodo), ruta)
                            .header("Authorization", login(CORREO.get(e.getKey())))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getStatus();
            if (e.getValue().equals("SI")) {
                assertNotEquals(403, estado, e.getKey() + " deberia poder usar " + metodo + " " + ruta);
                assertNotEquals(401, estado, e.getKey() + " deberia poder usar " + metodo + " " + ruta);
            } else {
                assertEquals(403, estado, e.getKey() + " NO deberia poder usar " + metodo + " " + ruta);
            }
        }
    }
}
