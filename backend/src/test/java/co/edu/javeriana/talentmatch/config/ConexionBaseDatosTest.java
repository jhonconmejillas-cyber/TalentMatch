package co.edu.javeriana.talentmatch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// Prueba de integracion: necesita DB_URL, DB_USER y DB_PASSWORD (variables de entorno o .env).
@SpringBootTest
class ConexionBaseDatosTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void laAplicacionSeConectaAPostgres() {
        // SELECT 1 es la consulta mas simple: si responde, la conexion funciona.
        assertEquals(1, jdbc.queryForObject("SELECT 1", Integer.class));
    }
}
