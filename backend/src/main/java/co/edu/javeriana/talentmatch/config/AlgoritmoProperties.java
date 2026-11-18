package co.edu.javeriana.talentmatch.config;

import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametros del algoritmo de matching, leidos de la seccion "algoritmo:" del YAML.
 *
 * En el YAML:                                  En Java:
 *   algoritmo:
 *     peso_habilidades: 0.7               ->     pesoHabilidades()
 *     peso_senioridad: 0.3                ->     pesoSenioridad()
 *     umbral_viabilidad: 65               ->     umbralViabilidad()
 *     ajuste_senioridad_superior: 0.9     ->     ajusteSenioridadSuperior()
 *     ajuste_senioridad_inferior: [...]   ->     ajusteSenioridadInferior()
 *
 * Spring convierte solo los nombres con guion bajo a camelCase.
 */
@ConfigurationProperties(prefix = "algoritmo") // "prefix" = nombre de la seccion en el YAML
public record AlgoritmoProperties(
        // Se usan Double/Integer (no double/int) a proposito:
        // si falta un valor en el YAML queda en null y lo detectamos abajo.
        // Con int quedaria en 0 sin avisar, y un umbral de 0 % seria un error grave.
        Double pesoHabilidades,
        Double pesoSenioridad,
        Integer umbralViabilidad,
        // RN-23: ajuste cuando el empleado SUPERA la senioridad de la vacante (0.9)
        Double ajusteSenioridadSuperior,
        // RN-23: ajuste segun cuantos niveles esta POR DEBAJO: [0 niveles, 1, 2, 3 o mas]
        List<Double> ajusteSenioridadInferior) {

    // Constructor compacto del record: se ejecuta al crear el objeto.
    // Aqui validamos; si algo esta mal, la aplicacion NO arranca
    // y muestra el mensaje (es mejor fallar al inicio que calcular mal despues).
    public AlgoritmoProperties {
        Objects.requireNonNull(pesoHabilidades, "Falta algoritmo.peso_habilidades en conf/config.yaml");
        Objects.requireNonNull(pesoSenioridad, "Falta algoritmo.peso_senioridad en conf/config.yaml");
        Objects.requireNonNull(umbralViabilidad, "Falta algoritmo.umbral_viabilidad en conf/config.yaml");
        Objects.requireNonNull(ajusteSenioridadSuperior, "Falta algoritmo.ajuste_senioridad_superior");
        Objects.requireNonNull(ajusteSenioridadInferior, "Falta algoritmo.ajuste_senioridad_inferior");

        // Match Score = habilidades * peso + senioridad * peso: los pesos deben sumar 1 (100 %).
        // Se compara con un margen (0.0001) porque los decimales en Java no son exactos (0.7 + 0.3 puede dar 0.9999999).
        if (Math.abs(pesoHabilidades + pesoSenioridad - 1.0) > 0.0001) {
            throw new IllegalStateException("peso_habilidades + peso_senioridad debe sumar 1.0");
        }

        // El umbral es un porcentaje (RN-27): debe estar entre 0 y 100.
        if (umbralViabilidad < 0 || umbralViabilidad > 100) {
            throw new IllegalStateException("umbral_viabilidad debe estar entre 0 y 100");
        }

        // Los ajustes son factores entre 0 y 1.
        ajusteSenioridadInferior = List.copyOf(ajusteSenioridadInferior);
        if (ajusteSenioridadInferior.isEmpty()
                || ajusteSenioridadInferior.stream().anyMatch(a -> a < 0 || a > 1)
                || ajusteSenioridadSuperior < 0 || ajusteSenioridadSuperior > 1) {
            throw new IllegalStateException("Los ajustes de senioridad deben estar entre 0 y 1");
        }
    }
}
