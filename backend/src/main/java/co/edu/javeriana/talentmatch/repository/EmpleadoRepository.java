package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Empleado;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a datos de {@link Empleado}. Solo lee y guarda: no contiene reglas de negocio. */
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    /** RF-10: empleados activos con alguna de estas habilidades en su wishlist. */
    @Query("SELECT DISTINCT e FROM Empleado e JOIN e.wishlist h WHERE h.id IN :habilidades AND e.activo = true")
    List<Empleado> conAlgunaEnWishlist(@Param("habilidades") Collection<Long> habilidades);

    List<Empleado> findByActivoTrue();
}
