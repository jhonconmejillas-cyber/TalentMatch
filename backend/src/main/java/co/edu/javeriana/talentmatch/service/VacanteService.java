package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.VacanteDtos.FiltroVacantes;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.PublicarVacanteRequest;
import co.edu.javeriana.talentmatch.dto.VacanteDtos.VacanteDto;
import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.Proyecto;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.HabilidadRepository;
import co.edu.javeriana.talentmatch.repository.ManagerRepository;
import co.edu.javeriana.talentmatch.repository.ProyectoRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Publicacion y busqueda de vacantes (RF-11, RF-13, CU_09, CU_10). */
@Service
public class VacanteService {

    private final VacanteRepository vacantes;
    private final ManagerRepository managers;
    private final HabilidadRepository habilidades;
    private final ProyectoRepository proyectos;
    private final Clock reloj;

    public VacanteService(VacanteRepository vacantes, ManagerRepository managers, HabilidadRepository habilidades,
                          ProyectoRepository proyectos, Clock reloj) {
        this.vacantes = vacantes;
        this.managers = managers;
        this.habilidades = habilidades;
        this.proyectos = proyectos;
        this.reloj = reloj;
    }

    /**
     * RF-11: el manager publica una vacante con habilidades criticas y deseables.
     * Una vacante publicada a mano queda ACTIVA de inmediato: el modelo es continuo (RN-01).
     */
    @Transactional
    public VacanteDto publicar(Long idManager, PublicarVacanteRequest datos) {
        Manager manager = managers.findById(idManager)
                .orElseThrow(() -> new AccesoDenegadoException("Solo un manager puede publicar vacantes."));
        LocalDateTime ahora = LocalDateTime.now(reloj);

        Set<Habilidad> criticas = buscarHabilidades(datos.habilidadesCriticasIds(), "habilidadesCriticasIds");
        Set<Habilidad> deseables = buscarHabilidades(datos.habilidadesDeseablesIds(), "habilidadesDeseablesIds");
        deseables.removeAll(criticas); // una habilidad no puede ser critica y deseable a la vez

        Vacante vacante = new Vacante(datos.titulo().trim(), datos.senioridad(), datos.area().trim(), manager, ahora);
        vacante.setDescripcion(datos.descripcion() == null ? null : datos.descripcion().trim());
        if (datos.proyectoId() != null) {
            Proyecto proyecto = proyectos.findById(datos.proyectoId()).filter(Proyecto::estaActivo)
                    .orElseThrow(() -> new DatosInvalidosException("proyectoId", "Selecciona un proyecto activo."));
            vacante.setProyecto(proyecto);
        }
        vacante.setHabilidadesCriticas(criticas);
        vacante.setHabilidadesDeseables(deseables);
        vacante.activar(ahora);
        return VacanteDto.de(vacantes.save(vacante));
    }

    /**
     * RF-13 / RN-11: busca entre las vacantes ACTIVAS de TODAS las areas (no solo la del empleado).
     * Las vacantes pendientes de activacion (RN-03) no aparecen.
     */
    @Transactional(readOnly = true)
    public List<VacanteDto> buscar(FiltroVacantes filtro) {
        return vacantes.findByEstadoOrderByFechaActivacionDesc(EstadoVacante.ACTIVA).stream()
                .filter(coincide(filtro))
                .map(VacanteDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public VacanteDto detalle(Long id) {
        return VacanteDto.de(obtener(id));
    }

    @Transactional(readOnly = true)
    public List<VacanteDto> deManager(Long idManager) {
        return vacantes.findByManagerIdOrderByFechaCreacionDesc(idManager).stream().map(VacanteDto::de).toList();
    }

    /** Todas las vacantes, opcionalmente de un estado (panel de RR.HH.). */
    @Transactional(readOnly = true)
    public List<VacanteDto> todas(EstadoVacante estado) {
        List<Vacante> lista = estado == null
                ? vacantes.findAll(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion"))
                : vacantes.findByEstadoOrderByFechaActivacionDesc(estado);
        return lista.stream().map(VacanteDto::de).toList();
    }

    Vacante obtener(Long id) {
        return vacantes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la vacante " + id + "."));
    }

    private static Predicate<Vacante> coincide(FiltroVacantes f) {
        Predicate<Vacante> p = v -> true;
        if (tieneTexto(f.titulo())) {
            String t = normalizar(f.titulo());
            p = p.and(v -> normalizar(v.getTitulo()).contains(t));
        }
        if (tieneTexto(f.area())) {
            String a = normalizar(f.area());
            p = p.and(v -> normalizar(v.getArea()).equals(a));
        }
        if (f.senioridad() != null) {
            p = p.and(v -> v.getSenioridad() == f.senioridad());
        }
        if (f.habilidadId() != null) {
            p = p.and(v -> v.habilidadesRequeridas().stream().anyMatch(h -> f.habilidadId().equals(h.getId())));
        }
        return p;
    }

    private Set<Habilidad> buscarHabilidades(List<Long> ids, String campo) {
        Set<Habilidad> resultado = new HashSet<>();
        if (ids == null) {
            return resultado;
        }
        for (Long id : ids) {
            resultado.add(habilidades.findById(id)
                    .orElseThrow(() -> new DatosInvalidosException(campo, "La habilidad " + id + " no existe en el catálogo.")));
        }
        return resultado;
    }

    private static boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }

    private static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
