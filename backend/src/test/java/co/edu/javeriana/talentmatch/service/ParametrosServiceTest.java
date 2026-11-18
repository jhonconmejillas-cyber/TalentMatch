package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.javeriana.talentmatch.config.AlertasProperties;
import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.config.PlazosProperties;
import co.edu.javeriana.talentmatch.config.ReporteEjecutivoProperties;
import org.junit.jupiter.api.Test;

// Prueba sin Spring: se crean los objetos a mano, como si vinieran del YAML.
class ParametrosServiceTest {

    @Test
    void entregaLosValoresDelArchivoDeConfiguracion() {
        ParametrosService parametros = new ParametrosService(
                new AlgoritmoProperties(0.7, 0.3, 65, 0.9, java.util.List.of(1.0, 0.6, 0.3, 0.0)),
                new PlazosProperties(6, 6, 24, 3, 10),
                new AlertasProperties(2),
                new ReporteEjecutivoProperties(10));

        assertEquals(65, parametros.umbralViabilidad());
        assertEquals(0.7, parametros.pesoHabilidades());
        assertEquals(6, parametros.antiguedadMinimaRolMeses());
        assertEquals(24, parametros.activacionDiferidaHoras());
        assertEquals(10, parametros.umbralVacanteSinMatchDias());
        assertEquals(2, parametros.rechazosViablesParaAlerta());
        assertEquals(10, parametros.topNReporteEjecutivo());
    }
}
