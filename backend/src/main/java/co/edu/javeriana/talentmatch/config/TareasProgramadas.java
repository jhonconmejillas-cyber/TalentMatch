package co.edu.javeriana.talentmatch.config;

import co.edu.javeriana.talentmatch.service.VacanteAutomaticaService;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tareas que el backend ejecuta solo (no son endpoints). Los horarios estan en
 * application.yml (talentmatch.tareas.*) y se pueden apagar con talentmatch.tareas.habilitadas=false.
 * La logica de cada tarea vive en la capa Service.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "talentmatch.tareas.habilitadas", havingValue = "true", matchIfMissing = true)
public class TareasProgramadas {

    private final VacanteAutomaticaService vacantes;
    private final Clock reloj;

    public TareasProgramadas(VacanteAutomaticaService vacantes, Clock reloj) {
        this.vacantes = vacantes;
        this.reloj = reloj;
    }

    /** RN-03: activa las vacantes cuya ventana de 24 h ya paso (cada minuto por defecto). */
    @Scheduled(cron = "${talentmatch.tareas.activacion-cron:0 * * * * *}")
    public void activarVacantesPendientes() {
        vacantes.activarPendientes(LocalDateTime.now(reloj));
    }

    /** RN-05 / RN-30: revisa las vacantes sin match (cada dia a las 7:00 por defecto). */
    @Scheduled(cron = "${talentmatch.tareas.vacantes-sin-match-cron:0 0 7 * * *}")
    public void revisarVacantesSinMatch() {
        vacantes.revisarVacantesSinMatch(LocalDateTime.now(reloj));
    }
}
