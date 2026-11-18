package co.edu.javeriana.talentmatch.rendimiento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * HU-005: prueba de carga con el servidor real (HTTP) y 50 usuarios concurrentes (RNF-01).
 *
 * Escenario 1: con 20 candidatos el ranking responde en menos de 2 s.
 * Escenario 2: con 50 usuarios en paralelo, el 90 % de las respuestas tarda menos de 3 s.
 *
 * Los datos de esta prueba se confirman en la base (el servidor corre en otro hilo) y se borran al final.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CargaConcurrenteTest {

    private static final int USUARIOS = 50;

    @LocalServerPort int puerto;
    @Autowired JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();
    private final List<Long> usuariosCreados = new ArrayList<>();

    @BeforeEach
    void cargarVeinteCandidatos() {
        for (int i = 0; i < 20; i++) {
            Long id = jdbc.queryForObject("""
                    INSERT INTO usuario (rol, nombre, correo, contrasena_hash) VALUES ('EMPLEADO', 'Carga', ?, 'x')
                    RETURNING id""", Long.class, "carga.concurrente" + i + "@talentmatch.com");
            jdbc.update("INSERT INTO empleado (id, senioridad) VALUES (?, 'SENIOR')", id);
            jdbc.update("""
                    INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final)
                    VALUES (?, 2, ?, ?)""", id, 65.0 + i, 65.0 + i);
            usuariosCreados.add(id);
        }
    }

    @AfterEach
    void limpiar() {
        for (Long id : usuariosCreados) {
            jdbc.update("DELETE FROM postulacion WHERE empleado_id = ?", id);
            jdbc.update("DELETE FROM usuario WHERE id = ?", id);
        }
    }

    private String token(String correo) throws Exception {
        HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url("/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"correo\":\"" + correo + "\",\"contrasena\":\"TalentMatch2026\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());
        Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(r.body());
        assertTrue(m.find(), r.body());
        return m.group(1);
    }

    private String url(String ruta) {
        return "http://localhost:" + puerto + "/api/v1" + ruta;
    }

    /** Devuelve la duracion en ms de un GET autenticado y verifica que responda 200. */
    private long medir(String token, String ruta) throws Exception {
        long inicio = System.nanoTime();
        HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(url(ruta)))
                .header("Authorization", "Bearer " + token).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, r.statusCode(), r.body());
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    @Test
    void escenario1_rankingDe20CandidatosEnMenosDe2Segundos() throws Exception {
        String manager = token("diana.torres@talentmatch.com");
        medir(manager, "/vacantes/2/ranking"); // calentamiento
        long ms = medir(manager, "/vacantes/2/ranking");
        assertTrue(ms < 2000, "El ranking tardo " + ms + " ms");
    }

    @Test
    void escenario2_50UsuariosConcurrentesEl90PorCientoEnMenosDe3Segundos() throws Exception {
        String manager = token("diana.torres@talentmatch.com");
        String empleado = token("sofia.castro@talentmatch.com");
        String rrhh = token("rrhh@talentmatch.com");

        List<Callable<Long>> tareas = new ArrayList<>();
        for (int i = 0; i < USUARIOS; i++) {
            int n = i;
            tareas.add(() -> switch (n % 3) {
                case 0 -> medir(manager, "/vacantes/2/ranking");
                case 1 -> medir(empleado, "/vacantes?titulo=engineer");
                default -> medir(rrhh, "/gestion/vacantes");
            });
        }
        ExecutorService hilos = Executors.newFixedThreadPool(USUARIOS);
        List<Long> tiempos = new ArrayList<>();
        try {
            for (Future<Long> f : hilos.invokeAll(tareas)) {
                tiempos.add(f.get()); // si alguna peticion fallo, la prueba falla aqui
            }
        } finally {
            hilos.shutdown();
        }
        Collections.sort(tiempos);
        long p90 = tiempos.get((int) Math.ceil(USUARIOS * 0.9) - 1);
        assertEquals(USUARIOS, tiempos.size(), "Todas las peticiones respondieron sin errores");
        assertTrue(p90 < 3000, "Percentil 90 = " + p90 + " ms; tiempos = " + tiempos);
    }
}
