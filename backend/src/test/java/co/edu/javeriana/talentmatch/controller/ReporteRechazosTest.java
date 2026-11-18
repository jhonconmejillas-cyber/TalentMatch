package co.edu.javeriana.talentmatch.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.talentmatch.ApiTest;
import co.edu.javeriana.talentmatch.service.ReporteRechazosService;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** HU-002: reporte mensual con datos simulados de 20 managers. */
class ReporteRechazosTest extends ApiTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired ReporteRechazosService servicio;

    private final String periodo = YearMonth.now().minusMonths(1).toString();
    private long managerSesgado;
    private long alertaSesgado;

    /** Fecha dentro del mes anterior. */
    private static final String FECHA = "date_trunc('month', CURRENT_DATE) - INTERVAL '10 days'";

    private long crearManager(int i) {
        long id = jdbc.queryForObject("""
                INSERT INTO usuario (rol, nombre, correo, contrasena_hash) VALUES ('MANAGER', ?, ?, 'x') RETURNING id""",
                Long.class, "Manager Simulado " + i, "msim" + i + "@talentmatch.com");
        jdbc.update("INSERT INTO empleado (id, senioridad) VALUES (?, 'LIDER')", id);
        return id;
    }

    private long vacante(long manager) {
        return jdbc.queryForObject("""
                INSERT INTO vacante (titulo, senioridad, area, estado, manager_id) VALUES ('Sim', 'SENIOR', 'X', 'CERRADA', ?)
                RETURNING id""", Long.class, manager);
    }

    private void resuelta(long empleado, long vacante, double puntaje, String estado, boolean directo) {
        jdbc.update("INSERT INTO postulacion (empleado_id, vacante_id, match_score, puntaje_final, estado, "
                + "fecha_creacion, fecha_resolucion, rechazo_directo) VALUES (?, ?, ?, ?, ?, " + FECHA + ", " + FECHA + ", ?)",
                empleado, vacante, puntaje, puntaje, estado, directo);
    }

    @BeforeEach
    void simular() {
        // 19 managers "normales": aceptan 1 y rechazan 1 de 4 viables (otros 2 quedan no seleccionados)
        for (int i = 1; i <= 19; i++) {
            long m = crearManager(i);
            long v = vacante(m);
            resuelta(8, v, 85, "ACEPTADA", false);
            resuelta(12, v, 80, "RECHAZADA", false);
            resuelta(13, v, 75, "RECHAZADA", false);
            resuelta(14, v, 60, "RECHAZADA", false); // no viable
        }
        // Manager 20: rechaza directamente a todos los viables en dos vacantes
        managerSesgado = crearManager(20);
        for (int k = 0; k < 2; k++) {
            long v = vacante(managerSesgado);
            resuelta(8, v, 90, "RECHAZADA", true);
            resuelta(12, v, 82, "RECHAZADA", true);
            resuelta(16, v, 78, "RECHAZADA", true);
        }
        alertaSesgado = jdbc.queryForObject("INSERT INTO alerta (tipo, manager_id, detalle, fecha) "
                + "VALUES ('RECHAZOS_REPETIDOS', ?, 'x', " + FECHA + ") RETURNING id", Long.class, managerSesgado);
    }

    @Test
    void escenario1y2_tasasPorManagerYResaltadoDeLaDesviacion() throws Exception {
        mvc.perform(get("/api/v1/reportes/rechazos?periodo=" + periodo).header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managers.length()").value(20))
                // El sesgado queda primero: rechazo a viables 100 %
                .andExpect(jsonPath("$.managers[0].managerId").value(managerSesgado))
                .andExpect(jsonPath("$.managers[0].tasaRechazoViables").value(100.0))
                .andExpect(jsonPath("$.managers[0].resaltado").value(true))
                .andExpect(jsonPath("$.managers[0].detalle.length()").value(6))
                .andExpect(jsonPath("$.managers[0].detalle[0].candidato", startsWith("Empleado_")))
                // Escenario 3: la alerta RN-29 queda enlazada
                .andExpect(jsonPath("$.managers[0].alertasRelacionadas[0]").value(alertaSesgado))
                // Un manager normal: 2 de 3 viables no seleccionados -> no resaltado
                .andExpect(jsonPath("$.managers[19].resaltado").value(false))
                .andExpect(jsonPath("$.managers[19].tasaAceptacion").value(25.0));
    }

    @Test
    void escenario3_elArchivoEsAnonimoYMarcaLasDesviaciones() throws Exception {
        String csv = mvc.perform(get("/api/v1/reportes/rechazos/archivo?periodo=" + periodo)
                        .header("Authorization", login(RRHH)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", startsWith("attachment")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(csv.contains(managerSesgado + ";6;0;6;0.0;100.0;6;6;100.0;"), csv);
        assertTrue(csv.contains(";SI;" + alertaSesgado));
        for (String nombre : List.of("Sofia", "Castro", "Isabella", "Mariana", "sofia.castro")) {
            assertFalse(csv.contains(nombre), "El reporte expone a " + nombre);
        }
    }

    @Test
    void elReporteSeGeneraUnaSolaVezPorMesYAvisaARrhh() {
        assertTrue(servicio.generarMesAnterior().isPresent());
        assertTrue(servicio.generarMesAnterior().isEmpty(), "No se duplica el reporte del mes");
        assertEquals(1, jdbc.queryForObject("SELECT anomalias FROM reporte_mensual WHERE periodo = ?",
                Integer.class, periodo));
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacion WHERE destinatario_id = 2 AND titulo LIKE 'Reporte mensual%'", Integer.class));
    }

    @Test
    void soloRrhhVeElReporte() throws Exception {
        mvc.perform(get("/api/v1/reportes/rechazos").header("Authorization", login(MANAGER_CARLOS)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/reportes/rechazos?periodo=octubre").header("Authorization", login(RRHH)))
                .andExpect(status().isBadRequest());
    }
}
