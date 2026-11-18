package co.edu.javeriana.talentmatch.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicacion. Los Services piden la hora a este objeto en lugar de
 * llamar a LocalDateTime.now(): asi las pruebas pueden "viajar en el tiempo"
 * (por ejemplo, simular que pasaron 6 meses o 24 horas) con un reloj fijo.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
