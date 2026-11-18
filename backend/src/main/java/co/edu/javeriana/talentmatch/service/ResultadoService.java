package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.ResultadoDtos.FilaComparacion;
import co.edu.javeriana.talentmatch.dto.ResultadoDtos.ResultadoDto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.Rol;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resultado del rechazo (CU_16, HU-003):
 * - RN-17: el manager no escribe nada; el resultado lo arma el sistema.
 * - RN-18: puntaje y habilidades del seleccionado, SIN ningun dato que lo identifique.
 * - RNF-08: la notificacion se genera en la misma transaccion de la decision (inmediata) y no
 *   bloquea el cierre de la vacante.
 * - RNF-10: solo el propio empleado y RR.HH. pueden ver el resultado.
 */
@Service
public class ResultadoService implements DecisionService.EventosDecision {

    private final PostulacionRepository postulaciones;
    private final UsuarioRepository usuarios;
    private final NotificacionService notificaciones;
    private final ParametrosService parametros;

    public ResultadoService(PostulacionRepository postulaciones, UsuarioRepository usuarios,
                            NotificacionService notificaciones, ParametrosService parametros) {
        this.postulaciones = postulaciones;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.parametros = parametros;
    }

    /** Rechazo directo: se avisa de inmediato; la comparacion llega cuando la vacante se cierre. */
    @Override
    public void alRechazar(Postulacion rechazada, boolean porDecisionDirecta, LocalDateTime ahora) {
        if (porDecisionDirecta) {
            notificaciones.notificar(rechazada.getEmpleado(), TipoNotificacion.RESULTADO_POSTULACION,
                    "No fuiste seleccionado en " + rechazada.getVacante().getTitulo(),
                    "Tu Puntaje Final fue " + formato(rechazada.getPuntajeFinal())
                            + ". Cuando la vacante se cierre podrás comparar tu perfil con el de la persona seleccionada.",
                    rechazada.getVacante(), rechazada);
        }
    }

    /** Al cerrarse la vacante, cada candidato no seleccionado recibe su resultado comparativo. */
    @Override
    public void alCerrarVacante(Vacante vacante, Postulacion aceptada, List<Postulacion> rechazadas,
                                LocalDateTime ahora) {
        postulaciones.findByVacanteIdAndEstado(vacante.getId(), EstadoPostulacion.RECHAZADA).forEach(p ->
                notificaciones.notificar(p.getEmpleado(), TipoNotificacion.RESULTADO_POSTULACION,
                        "Resultado de tu postulación a " + vacante.getTitulo(),
                        "Tu Puntaje Final: " + formato(p.getPuntajeFinal()) + " · Puntaje del seleccionado: "
                                + formato(aceptada.getPuntajeFinal())
                                + ". Abre el resultado para ver qué habilidades marcaron la diferencia.",
                        vacante, p));
    }

    @Transactional(readOnly = true)
    public ResultadoDto resultado(Long idUsuario, Long idPostulacion) {
        Postulacion mia = postulaciones.findById(idPostulacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la postulación " + idPostulacion + "."));
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("Sesión no válida."));
        // RNF-10: el historial de rechazos solo lo ven el propio empleado y RR.HH.
        if (!usuario.mismoUsuarioQue(mia.getEmpleado()) && usuario.getRol() != Rol.RRHH) {
            throw new AccesoDenegadoException("Solo tú y RR.HH. pueden ver el resultado de tus postulaciones.");
        }
        if (mia.getEstado() != EstadoPostulacion.RECHAZADA) {
            throw new RecursoNoEncontradoException("Esta postulación no tiene un resultado de rechazo.");
        }
        Vacante vacante = mia.getVacante();
        Empleado yo = mia.getEmpleado();
        Optional<Postulacion> seleccionada = postulaciones
                .findByVacanteIdAndEstado(vacante.getId(), EstadoPostulacion.ACEPTADA).stream().findFirst();

        Set<Habilidad> mias = Set.copyOf(yo.habilidadesAvaladas());
        Set<Habilidad> suyas = seleccionada.map(p -> Set.copyOf(p.getEmpleado().habilidadesAvaladas())).orElse(Set.of());
        Set<Habilidad> wishlist = yo.getWishlist();

        Set<Habilidad> filas = new LinkedHashSet<>();
        vacante.habilidadesRequeridas().stream().sorted(Comparator.comparing(Habilidad::getNombre)).forEach(filas::add);
        suyas.stream().sorted(Comparator.comparing(Habilidad::getNombre)).forEach(filas::add);
        List<FilaComparacion> comparacion = filas.stream()
                .map(h -> new FilaComparacion(h.getNombre(), mias.contains(h), suyas.contains(h), wishlist.contains(h),
                        h.getId()))
                .toList();

        LocalDate desde = mia.getFechaResolucion().toLocalDate().plusMonths(parametros.bloqueoRepostulacionMeses());
        String queSigue = "Podrás volver a postularte a este puesto a partir del " + desde
                + ", siempre que se abra una nueva rotación en él. Agrega a tu wishlist las habilidades que te faltan.";
        return new ResultadoDto(mia.getId(), vacante.getTitulo(), mia.getPuntajeFinal(),
                seleccionada.map(Postulacion::getPuntajeFinal).orElse(null),
                mia.getPosicionRanking(), mia.getTotalCandidatos(), comparacion, desde, queSigue);
    }

    private static String formato(double puntaje) {
        return String.format(Locale.ROOT, "%.1f %%", puntaje);
    }
}
