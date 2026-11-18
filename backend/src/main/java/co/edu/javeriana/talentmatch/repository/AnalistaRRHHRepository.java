package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.AnalistaRRHH;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Analistas de RR.HH. activos (destinatarios de alertas y avisos de rotacion). */
public interface AnalistaRRHHRepository extends JpaRepository<AnalistaRRHH, Long> {

    List<AnalistaRRHH> findByActivoTrue();
}
