package co.edu.javeriana.talentmatch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest arranca la aplicacion completa para la prueba,
// igual que cuando le das al boton de ejecutar (incluye leer el YAML).
@SpringBootTest
class ConfiguracionTest {

    // @Autowired: Spring nos entrega los objetos ya llenos con el YAML.
    @Autowired AlgoritmoProperties algoritmo;
    @Autowired PlazosProperties plazos;

    @Test
    void cargaLosParametrosDesdeConfigYaml() {
        // assertEquals(esperado, real): si no coinciden, la prueba falla.
        // Los valores esperados son los de conf/config.example.yaml.
        assertEquals(65, algoritmo.umbralViabilidad());
        assertEquals(0.7, algoritmo.pesoHabilidades());
        assertEquals(6, plazos.antiguedadMinimaRolMeses());
        assertEquals(24, plazos.activacionDiferidaHoras());
    }
}