package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.MatchingDtos.DesgloseDto;
import co.edu.javeriana.talentmatch.dto.MatchingDtos.MiPuntajeDto;
import co.edu.javeriana.talentmatch.model.BitacoraCalculo;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.BitacoraCalculoRepository;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Entrada;
import co.edu.javeriana.talentmatch.service.CalculadoraMatchScore.Resultado;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calculo del Puntaje Final (CU_11, RF-15).
 *
 *   Puntaje Final = Match Score   (RN-25)
 *
 * Garantias:
 * - Solo habilidades AVALADAS (RN-20) y ningun dato personal (RNF-09): ver {@link CalculadoraMatchScore}.
 * - Cada calculo se guarda en la bitacora ANTES de devolverse. Si la bitacora falla,
 *   el calculo falla y no se devuelve ningun puntaje (RNF-06, excepcion del paso 6 de CU_11).
 */
@Service
public class MatchingService {

    private final CalculadoraMatchScore calculadora;
    private final BitacoraCalculoRepository bitacora;
    private final EmpleadoRepository empleados;
    private final VacanteRepository vacantes;
    private final ParametrosService parametros;
    private final Clock reloj;

    public MatchingService(CalculadoraMatchScore calculadora, BitacoraCalculoRepository bitacora,
                           EmpleadoRepository empleados, VacanteRepository vacantes,
                           ParametrosService parametros, Clock reloj) {
        this.calculadora = calculadora;
        this.bitacora = bitacora;
        this.empleados = empleados;
        this.vacantes = vacantes;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    /** Puntaje calculado y ya registrado en la bitacora. */
    public record Calculo(Resultado resultado, double puntajeFinal, double umbral) {

        public boolean viable() {
            return puntajeFinal >= umbral;
        }
    }

    /** Calcula, registra en la bitacora y devuelve. Debe ejecutarse dentro de una transaccion. */
    @Transactional
    public Calculo calcular(Empleado empleado, Vacante vacante) {
        List<HabilidadEmpleado> habilidades = empleado.getHabilidades();
        List<Habilidad> avaladas = habilidades.stream()
                .filter(HabilidadEmpleado::cuentaParaCalculo).map(HabilidadEmpleado::getHabilidad).toList();
        List<Habilidad> noAvaladas = habilidades.stream()
                .filter(h -> !h.cuentaParaCalculo()).map(HabilidadEmpleado::getHabilidad).toList();

        Resultado resultado = calculadora.calcular(new Entrada(avaladas, noAvaladas, empleado.getSenioridad(),
                vacante.habilidadesRequeridas(), vacante.getSenioridad()));
        double puntajeFinal = resultado.matchScore(); // RN-25
        double umbral = parametros.umbralViabilidad();

        // RNF-06: primero la bitacora; si falla, la excepcion sube y no se devuelve el puntaje.
        bitacora.save(new BitacoraCalculo(empleado, vacante, resultado.matchScore(), puntajeFinal, umbral,
                LocalDateTime.now(reloj), resultado.detalle()));

        return new Calculo(resultado, puntajeFinal, umbral);
    }

    /** RF-16 / RF-17: el empleado ve su puntaje y desglose antes de postularse. */
    @Transactional
    public MiPuntajeDto miPuntaje(Long idEmpleado, Long idVacante) {
        Empleado empleado = empleados.findById(idEmpleado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado " + idEmpleado + "."));
        Vacante vacante = vacantes.findById(idVacante)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la vacante " + idVacante + "."));
        Calculo calculo = calcular(empleado, vacante);
        return aDto(vacante.getId(), calculo);
    }

    public static MiPuntajeDto aDto(Long idVacante, Calculo c) {
        String aviso = c.viable() ? null : String.format(java.util.Locale.ROOT,
                "Tu Puntaje Final (%.1f %%) está por debajo del umbral de %.0f %%: tus posibilidades son bajas "
                        + "y no entrarás al ranking del manager (verá tu postulación en una lista aparte). "
                        + "Aun así puedes postularte.",
                c.puntajeFinal(), c.umbral());
        return new MiPuntajeDto(idVacante, c.resultado().matchScore(), c.puntajeFinal(), c.umbral(), c.viable(),
                aviso, DesgloseDto.de(c.resultado()));
    }
}
