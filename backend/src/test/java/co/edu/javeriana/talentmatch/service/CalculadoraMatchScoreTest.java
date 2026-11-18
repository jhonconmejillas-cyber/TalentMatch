package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Entrada;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Resultado;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Pruebas exhaustivas de la formula del Match Score (RN-23, RN-20, RNF-09). */
class CalculadoraMatchScoreTest {

    private static final Habilidad JAVA = new Habilidad(1L, "Java");
    private static final Habilidad SQL = new Habilidad(3L, "SQL");
    private static final Habilidad DOCKER = new Habilidad(5L, "Docker");
    private static final Habilidad KUBERNETES = new Habilidad(6L, "Kubernetes");
    private static final Habilidad GO = new Habilidad(7L, "Go");
    private static final Habilidad AWS = new Habilidad(9L, "AWS");

    private final CalculadoraMatchScore calculadora = new CalculadoraMatchScore(
            new AlgoritmoProperties(0.7, 0.3, 65, 0.9, List.of(1.0, 0.6, 0.3, 0.0)));

    private Resultado calcular(List<Habilidad> avaladas, List<Habilidad> noAvaladas, Senioridad propia,
                               Set<Habilidad> requeridas, Senioridad vacante) {
        return calculadora.calcular(new Entrada(avaladas, noAvaladas, propia, requeridas, vacante));
    }

    @Test
    void ejemploTrazableDeLasReglasDeNegocio() {
        // RN §7: Juan (Senior) con Java, SQL, Docker avaladas; vacante Semi Senior con Go, Kubernetes, Docker.
        Resultado r = calcular(List.of(JAVA, SQL, DOCKER), List.of(), Senioridad.SENIOR,
                Set.of(GO, KUBERNETES, DOCKER), Senioridad.SEMI_SENIOR);

        assertEquals(50.3, r.matchScore(), 0.001); // (0.333 x 0.7) + (0.9 x 0.3) ~= 50 %
        assertEquals(List.of("Docker"), r.coincidentes());
        assertEquals(List.of("Go", "Kubernetes"), r.faltantes());
        assertEquals(0.9, r.ajusteSenioridad());
    }

    @Test
    void hu007_lasHabilidadesAgregadasNoCambianElPuntaje() {
        // Escenario 3: 5 habilidades agregadas y 2 avaladas -> solo cuentan las 2 avaladas.
        Set<Habilidad> requeridas = Set.of(JAVA, SQL, DOCKER, KUBERNETES, GO);
        Resultado soloAvaladas = calcular(List.of(JAVA, SQL), List.of(), Senioridad.SENIOR,
                requeridas, Senioridad.SENIOR);
        Resultado conAgregadas = calcular(List.of(JAVA, SQL), List.of(DOCKER, KUBERNETES, GO), Senioridad.SENIOR,
                requeridas, Senioridad.SENIOR);

        assertEquals(soloAvaladas.matchScore(), conAgregadas.matchScore());
        assertEquals(58.0, conAgregadas.matchScore(), 0.001); // 2/5 x 0.7 + 1 x 0.3
        // El desglose explica por que no contaron
        assertEquals(List.of("Docker", "Go", "Kubernetes"), conAgregadas.sinAvalar());
    }

    @Test
    void todasLasHabilidadesYMismaSenioridadDan100() {
        Resultado r = calcular(List.of(JAVA, SQL), List.of(), Senioridad.SENIOR, Set.of(JAVA, SQL), Senioridad.SENIOR);
        assertEquals(100.0, r.matchScore());
    }

    @Test
    void ningunaHabilidadYMuyPorDebajoDa0() {
        Resultado r = calcular(List.of(), List.of(), Senioridad.JUNIOR, Set.of(JAVA), Senioridad.LIDER);
        assertEquals(0.0, r.matchScore());
    }

    @Test
    void unaVacanteSinHabilidadesSoloDependeDeLaSenioridad() {
        Resultado r = calcular(List.of(), List.of(), Senioridad.SEMI_SENIOR, Set.of(), Senioridad.SENIOR);
        assertEquals(88.0, r.matchScore(), 0.001); // 1.0 x 0.7 + 0.6 x 0.3
    }

    @ParameterizedTest(name = "empleado {0} vs vacante {1} -> ajuste {2}")
    @CsvSource({
        "SENIOR, SENIOR, 1.0",
        "LIDER, JUNIOR, 0.9",
        "SENIOR, SEMI_SENIOR, 0.9",
        "SEMI_SENIOR, SENIOR, 0.6",
        "JUNIOR, SENIOR, 0.3",
        "JUNIOR, LIDER, 0.0",
    })
    void ajusteDeSenioridad(Senioridad propia, Senioridad vacante, double esperado) {
        assertEquals(esperado, calculadora.ajusteSenioridad(propia, vacante));
    }

    @Test
    void faltarUnaODosTecnologiasNoPenalizaFuerte() {
        // RN-23: con 4 de 5 habilidades y la misma senioridad el candidato sigue siendo viable (>= 65).
        Resultado r = calcular(List.of(JAVA, SQL, DOCKER, KUBERNETES), List.of(), Senioridad.SENIOR,
                Set.of(JAVA, SQL, DOCKER, KUBERNETES, AWS), Senioridad.SENIOR);
        assertTrue(r.matchScore() >= 65, "Puntaje: " + r.matchScore());
    }

    @Test
    void rnf09_laEntradaEsUnaListaCerradaSinDatosPersonales() {
        // Si alguien agrega un campo a la entrada del calculo, esta prueba falla y obliga a revisarlo.
        List<String> campos = Arrays.stream(Entrada.class.getRecordComponents()).map(RecordComponent::getName).toList();
        assertEquals(List.of("habilidadesAvaladas", "habilidadesNoAvaladas", "senioridadEmpleado",
                "habilidadesRequeridas", "senioridadVacante"), campos);
    }

    @Test
    void elResultadoEsElMismoSinImportarElOrden() {
        Resultado a = calcular(List.of(JAVA, DOCKER), List.of(), Senioridad.SENIOR, Set.of(JAVA, DOCKER, GO), Senioridad.SENIOR);
        Resultado b = calcular(List.of(DOCKER, JAVA), List.of(), Senioridad.SENIOR, Set.of(GO, DOCKER, JAVA), Senioridad.SENIOR);
        assertEquals(a.matchScore(), b.matchScore());
    }
}
