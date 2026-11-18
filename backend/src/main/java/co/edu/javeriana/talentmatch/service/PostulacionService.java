package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.PostulacionDtos.ElegibilidadDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.MiPostulacionDto;
import co.edu.javeriana.talentmatch.dto.PostulacionDtos.PostulacionCreadaDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.ReglaNegocioException;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import co.edu.javeriana.talentmatch.service.MatchingService.Calculo;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Postulacion continua a vacantes (CU_12, CU_17, HU-008):
 * - RN-06 / RN-08: sin fechas limite y con varias postulaciones activas a la vez.
 * - RN-07: minimo 6 meses en el rol actual.
 * - RN-10: tras un rechazo, 6 meses de espera Y una nueva rotacion en ese mismo puesto.
 * - RN-09: al aceptar una, las demas se cancelan sin avisar a sus managers.
 * - RN-28: un puntaje bajo NO bloquea; solo se avisa.
 */
@Service
public class PostulacionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
            Locale.forLanguageTag("es-CO"));

    private final PostulacionRepository postulaciones;
    private final EmpleadoRepository empleados;
    private final VacanteRepository vacantes;
    private final MatchingService matching;
    private final ParametrosService parametros;
    private final Clock reloj;

    public PostulacionService(PostulacionRepository postulaciones, EmpleadoRepository empleados,
                              VacanteRepository vacantes, MatchingService matching, ParametrosService parametros,
                              Clock reloj) {
        this.postulaciones = postulaciones;
        this.empleados = empleados;
        this.vacantes = vacantes;
        this.matching = matching;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    /** Resultado de revisar las reglas antes de postularse. */
    record Bloqueo(String motivo, LocalDate fechaHabilitacion) {
    }

    @Transactional(readOnly = true)
    public ElegibilidadDto elegibilidad(Long idEmpleado, Long idVacante) {
        Empleado empleado = empleado(idEmpleado);
        Vacante vacante = vacante(idVacante);
        boolean yaPostulado = postulaciones.existsByEmpleadoIdAndVacanteIdAndEstado(
                idEmpleado, idVacante, EstadoPostulacion.ACTIVA);
        Optional<Bloqueo> bloqueo = revisarReglas(empleado, vacante, yaPostulado);
        return bloqueo.map(b -> new ElegibilidadDto(false, b.motivo(), b.fechaHabilitacion(), yaPostulado))
                .orElseGet(() -> new ElegibilidadDto(true, null, null, false));
    }

    /** RF-19 / RF-20: crea la postulacion, calcula el Puntaje Final (con bitacora) y avisa si es bajo. */
    @Transactional
    public PostulacionCreadaDto postularse(Long idEmpleado, Long idVacante) {
        Empleado empleado = empleado(idEmpleado);
        Vacante vacante = vacante(idVacante);
        boolean yaPostulado = postulaciones.existsByEmpleadoIdAndVacanteIdAndEstado(
                idEmpleado, idVacante, EstadoPostulacion.ACTIVA);
        revisarReglas(empleado, vacante, yaPostulado).ifPresent(b -> {
            throw new ReglaNegocioException(b.motivo());
        });

        Postulacion postulacion = new Postulacion(empleado, vacante, LocalDateTime.now(reloj));
        Calculo calculo = matching.calcular(empleado, vacante);
        postulacion.registrarPuntaje(calculo.puntajeFinal());
        postulaciones.save(postulacion);

        String aviso = MatchingService.aDto(vacante.getId(), calculo).aviso();
        return new PostulacionCreadaDto(MiPostulacionDto.de(postulacion), calculo.viable(), aviso);
    }

    @Transactional(readOnly = true)
    public List<MiPostulacionDto> misPostulaciones(Long idEmpleado) {
        return postulaciones.findByEmpleadoIdOrderByFechaCreacionDesc(idEmpleado).stream()
                .map(MiPostulacionDto::de).toList();
    }

    /**
     * RN-09: al aceptarse {@code aceptada}, las demas postulaciones activas del empleado pasan a
     * CANCELADA_POR_ROTACION. No se genera ninguna notificacion para los managers de esas vacantes:
     * el candidato simplemente deja de aparecer en su ranking.
     */
    @Transactional
    public List<Postulacion> cancelarOtrasPostulaciones(Postulacion aceptada, LocalDateTime ahora) {
        List<Postulacion> otras = postulaciones
                .findByEmpleadoIdAndEstado(aceptada.getEmpleado().getId(), EstadoPostulacion.ACTIVA).stream()
                .filter(p -> !p.getId().equals(aceptada.getId()))
                .toList();
        otras.forEach(p -> p.cancelarPorRotacion(ahora));
        postulaciones.saveAll(otras);
        return otras;
    }

    /** Reglas de RN-07 y RN-10 (y basicas: vacante activa, no repetir, no la propia). */
    Optional<Bloqueo> revisarReglas(Empleado empleado, Vacante vacante, boolean yaPostulado) {
        LocalDate hoy = LocalDate.now(reloj);
        if (!vacante.estaActiva()) {
            return Optional.of(new Bloqueo("La vacante no está recibiendo postulaciones.", null));
        }
        if (yaPostulado) {
            return Optional.of(new Bloqueo("Ya tienes una postulación activa a esta vacante.", null));
        }
        if (empleado.mismoUsuarioQue(vacante.getManager())) {
            return Optional.of(new Bloqueo("No puedes postularte a una vacante que tú publicaste.", null));
        }

        // RN-07: antiguedad minima en el rol actual
        int meses = parametros.antiguedadMinimaRolMeses();
        if (empleado.getFechaInicioRol() == null) {
            return Optional.of(new Bloqueo("Registra en tu perfil la fecha de inicio en tu rol actual para poder postularte.", null));
        }
        LocalDate habilitadoPorRol = empleado.fechaHabilitacionPorAntiguedad(meses);
        if (hoy.isBefore(habilitadoPorRol)) {
            return Optional.of(new Bloqueo("Podrás postularte a partir del " + FECHA.format(habilitadoPorRol)
                    + ": se requieren " + meses + " meses en el rol actual.", habilitadoPorRol));
        }

        // RN-10: rechazos previos en el mismo puesto
        int bloqueo = parametros.bloqueoRepostulacionMeses();
        List<Postulacion> rechazosMismoPuesto = postulaciones
                .findByEmpleadoIdAndEstado(empleado.getId(), EstadoPostulacion.RECHAZADA).stream()
                .filter(p -> p.getVacante().esMismoPuestoQue(vacante) || p.getVacante().getId().equals(vacante.getId()))
                .sorted(Comparator.comparing(Postulacion::getFechaResolucion).reversed())
                .toList();
        if (!rechazosMismoPuesto.isEmpty()) {
            Postulacion ultimo = rechazosMismoPuesto.get(0);
            LocalDate habilitado = ultimo.getFechaResolucion().toLocalDate().plusMonths(bloqueo);
            if (ultimo.getVacante().getId().equals(vacante.getId())) {
                return Optional.of(new Bloqueo("No fuiste seleccionado en esta vacante. Podrás volver a postularte a este puesto "
                        + "cuando se abra una nueva rotación en él, a partir del " + FECHA.format(habilitado) + ".", habilitado));
            }
            if (hoy.isBefore(habilitado)) {
                return Optional.of(new Bloqueo("Podrás postularte a este puesto a partir del " + FECHA.format(habilitado)
                        + ": deben pasar " + bloqueo + " meses desde que no fuiste seleccionado en él.", habilitado));
            }
        }
        return Optional.empty();
    }

    private Empleado empleado(Long id) {
        return empleados.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado " + id + "."));
    }

    private Vacante vacante(Long id) {
        return vacantes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la vacante " + id + "."));
    }
}
