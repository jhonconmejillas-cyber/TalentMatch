package co.edu.javeriana.talentmatch.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Entrada;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Resultado;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Plan de pruebas — Componente 1: CalculadoraMatchScore (RN-23, RN-20, RNF-09).
 * Pesos del taller: habilidades 0.7, senioridad 0.3; ajuste superior 0.9; inferior [1.0, 0.6, 0.3, 0.0].
 * Cada prueba sigue Arrange - Act - Assert y no usa base de datos ni Spring (F.I.R.S.T).
 */
@Tag("plan-pruebas")
@DisplayName("Componente 1 · CalculadoraMatchScore")
class CalculadoraMatchScorePlanTest {

    private static final Habilidad JAVA = new Habilidad(1L, "Java");
    private static final Habilidad SQL = new Habilidad(2L, "SQL");
    private static final Habilidad DOCKER = new Habilidad(3L, "Docker");

    private final CalculadoraMatchScore calculadora = new CalculadoraMatchScore(
            new AlgoritmoProperties(0.7, 0.3, 65, 0.9, List.of(1.0, 0.6, 0.3, 0.0)));

    @Test
    @DisplayName("CP01 | Normal | calcular: todas las habilidades avaladas y misma senioridad dan 100")
    void cp01_coincidenciaTotal() {
        // Arrange
        Entrada entrada = new Entrada(List.of(JAVA, SQL), List.of(), Senioridad.SENIOR,
                Set.of(JAVA, SQL), Senioridad.SENIOR);
        // Act
        Resultado r = calculadora.calcular(entrada);
        // Assert
        assertAll(
                () -> assertEquals(100.0, r.matchScore()),
                () -> assertEquals(1.0, r.porcentajeHabilidades()),
                () -> assertEquals(List.of(), r.faltantes()));
    }

    @Test
    @DisplayName("CP02 | Normal | porcentaje de habilidades: 1 de 2 avaladas da 0.5 y Match Score 65")
    void cp02_porcentajeParcial() {
        // Arrange
        Entrada entrada = new Entrada(List.of(JAVA), List.of(), Senioridad.SENIOR,
                Set.of(JAVA, SQL), Senioridad.SENIOR);
        // Act
        Resultado r = calculadora.calcular(entrada);
        // Assert: (0.5 x 0.7 + 1.0 x 0.3) x 100 = 65
        assertAll(
                () -> assertEquals(0.5, r.porcentajeHabilidades()),
                () -> assertEquals(65.0, r.matchScore()),
                () -> assertEquals(List.of("SQL"), r.faltantes()));
    }

    @Test
    @DisplayName("CP03 | Borde | calcular: vacante sin habilidades requeridas cuenta el 100 % de habilidades")
    void cp03_vacanteSinHabilidades() {
        // Arrange
        Entrada entrada = new Entrada(List.of(), List.of(), Senioridad.SENIOR, Set.of(), Senioridad.SENIOR);
        // Act
        Resultado r = calculadora.calcular(entrada);
        // Assert
        assertEquals(1.0, r.porcentajeHabilidades());
        assertEquals(100.0, r.matchScore());
    }

    @Test
    @DisplayName("CP04 | Borde | ajusteSenioridad: 3 niveles por debajo (Junior vs Lider) da 0.0")
    void cp04_senioridadMinima() {
        // Arrange / Act
        double ajuste = calculadora.ajusteSenioridad(Senioridad.JUNIOR, Senioridad.LIDER);
        // Assert
        assertEquals(0.0, ajuste);
    }

    @Test
    @DisplayName("CP05 | Borde | ajusteSenioridad: sin senioridad declarada se toma como Junior")
    void cp05_senioridadNula() {
        // Arrange / Act
        double sinDeclarar = calculadora.ajusteSenioridad(null, Senioridad.SEMI_SENIOR);
        double junior = calculadora.ajusteSenioridad(Senioridad.JUNIOR, Senioridad.SEMI_SENIOR);
        // Assert
        assertEquals(junior, sinDeclarar);
        assertEquals(0.6, sinDeclarar);
    }

    @Test
    @DisplayName("CP06 | Negativa | calcular: habilidades avaladas nulas lanzan NullPointerException")
    void cp06_habilidadesNulas() {
        // Arrange
        Entrada entrada = new Entrada(null, List.of(), Senioridad.SENIOR, Set.of(JAVA), Senioridad.SENIOR);
        // Act / Assert
        assertThrows(NullPointerException.class, () -> calculadora.calcular(entrada));
    }

    @Test
    @DisplayName("CP07 | Negativa | calcular: habilidades requeridas nulas lanzan NullPointerException")
    void cp07_requeridasNulas() {
        // Arrange
        Entrada entrada = new Entrada(List.of(JAVA), List.of(), Senioridad.SENIOR, null, Senioridad.SENIOR);
        // Act / Assert
        assertThrows(NullPointerException.class, () -> calculadora.calcular(entrada));
    }

    @Test
    @DisplayName("CP08 | Negocio | RN-20: una habilidad declarada pero sin avalar no suma puntaje")
    void cp08_habilidadSinAvalarNoCuenta() {
        // Arrange: tiene Docker declarado pero el manager no lo ha avalado
        Entrada entrada = new Entrada(List.of(JAVA), List.of(DOCKER), Senioridad.SENIOR,
                Set.of(JAVA, DOCKER), Senioridad.SENIOR);
        // Act
        Resultado r = calculadora.calcular(entrada);
        // Assert
        assertAll(
                () -> assertEquals(0.5, r.porcentajeHabilidades()),
                () -> assertEquals(List.of("Docker"), r.sinAvalar()),
                () -> assertEquals(List.of("Docker"), r.faltantes()));
    }

    @Test
    @DisplayName("CP09 | Normal | Resultado.detalle: el texto de la bitacora incluye la formula aplicada")
    void cp09_detalleParaBitacora() {
        // Arrange
        Resultado r = calculadora.calcular(new Entrada(List.of(JAVA), List.of(), Senioridad.SENIOR,
                Set.of(JAVA, SQL), Senioridad.SENIOR));
        // Act
        String detalle = r.detalle();
        // Assert
        assertTrue(detalle.contains("coinciden 1 de 2"), detalle);
        assertTrue(detalle.contains("= 65.0"), detalle);
    }

    @Test
    @DisplayName("CP10 | Normal | ajusteSenioridad: superar la senioridad pedida aplica el factor 0.9")
    void cp10_senioridadSuperior() {
        // Arrange / Act
        double ajuste = calculadora.ajusteSenioridad(Senioridad.LIDER, Senioridad.SENIOR);
        // Assert
        assertEquals(0.9, ajuste);
    }
}
