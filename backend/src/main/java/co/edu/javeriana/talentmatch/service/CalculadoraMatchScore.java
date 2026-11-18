package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Senioridad;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Formula del Match Score (RN-23), sin IA y sin base de datos:
 *
 *   Match Score = (% habilidades avaladas coincidentes x peso_habilidades)
 *               + (ajuste por senioridad x peso_senioridad)
 *
 * NEUTRALIDAD (RNF-09): la formula recibe una LISTA CERRADA de datos:
 * las habilidades AVALADAS del empleado, su senioridad y los requisitos de la vacante.
 * No recibe el objeto Empleado, asi que el nombre, la edad, el area, la foto o la
 * relacion con el manager no pueden influir aunque alguien lo intente.
 */
@Component
public class CalculadoraMatchScore {

    private final AlgoritmoProperties algoritmo;

    public CalculadoraMatchScore(AlgoritmoProperties algoritmo) {
        this.algoritmo = algoritmo;
    }

    /** Unicos datos que entran al calculo (RNF-09). */
    public record Entrada(
            Collection<Habilidad> habilidadesAvaladas,
            Collection<Habilidad> habilidadesNoAvaladas,
            Senioridad senioridadEmpleado,
            Set<Habilidad> habilidadesRequeridas,
            Senioridad senioridadVacante) {
    }

    /** Resultado con el desglose que se le explica al empleado (RF-16). */
    public record Resultado(
            double matchScore,
            double porcentajeHabilidades,
            double ajusteSenioridad,
            double pesoHabilidades,
            double pesoSenioridad,
            List<String> coincidentes,
            List<String> faltantes,
            /** Requeridas que el empleado tiene, pero sin avalar: no cuentan (RN-20). */
            List<String> sinAvalar) {

        /** Texto corto del calculo para la bitacora (RNF-06). */
        public String detalle() {
            return String.format(java.util.Locale.ROOT,
                    "habilidades=%.4f (coinciden %s de %s) x %.2f + senioridad=%.2f x %.2f = %.1f; sin avalar=%s",
                    porcentajeHabilidades, coincidentes.size(), coincidentes.size() + faltantes.size(),
                    pesoHabilidades, ajusteSenioridad, pesoSenioridad, matchScore, sinAvalar);
        }
    }

    public Resultado calcular(Entrada e) {
        Set<Habilidad> avaladas = Set.copyOf(e.habilidadesAvaladas());
        List<String> coincidentes = e.habilidadesRequeridas().stream()
                .filter(avaladas::contains).map(Habilidad::getNombre).sorted().toList();
        List<String> faltantes = e.habilidadesRequeridas().stream()
                .filter(h -> !avaladas.contains(h)).map(Habilidad::getNombre).sorted().toList();
        Set<Habilidad> noAvaladas = Set.copyOf(e.habilidadesNoAvaladas());
        List<String> sinAvalar = e.habilidadesRequeridas().stream()
                .filter(h -> !avaladas.contains(h) && noAvaladas.contains(h))
                .map(Habilidad::getNombre).sorted().toList();

        double porcentaje = e.habilidadesRequeridas().isEmpty() ? 1.0
                : (double) coincidentes.size() / e.habilidadesRequeridas().size();
        double ajuste = ajusteSenioridad(e.senioridadEmpleado(), e.senioridadVacante());
        double score = (porcentaje * algoritmo.pesoHabilidades() + ajuste * algoritmo.pesoSenioridad()) * 100;

        return new Resultado(redondear(score), porcentaje, ajuste, algoritmo.pesoHabilidades(),
                algoritmo.pesoSenioridad(), coincidentes, faltantes, sinAvalar);
    }

    /**
     * Ajuste por senioridad (RN-23): 1.0 si coincide, el factor "superior" si la supera
     * (no penaliza fuerte) y un factor decreciente por cada nivel por debajo.
     * Sin senioridad declarada se toma como el nivel mas bajo.
     */
    double ajusteSenioridad(Senioridad empleado, Senioridad vacante) {
        if (vacante == null) {
            return 1.0;
        }
        Senioridad propia = empleado == null ? Senioridad.JUNIOR : empleado;
        int diferencia = propia.diferenciaCon(vacante);
        if (diferencia > 0) {
            return algoritmo.ajusteSenioridadSuperior();
        }
        List<Double> tabla = algoritmo.ajusteSenioridadInferior();
        return tabla.get(Math.min(-diferencia, tabla.size() - 1));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
