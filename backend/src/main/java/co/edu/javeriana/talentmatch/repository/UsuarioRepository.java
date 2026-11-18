package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a datos de {@link Usuario}. Solo lee y guarda: no contiene reglas de negocio. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    /**
     * Cambia la columna "rol" (que define la subclase Java). JPA no permite cambiar
     * la clase de un objeto, por eso se usa SQL directo y luego se limpia la cache.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE usuario SET rol = :rol WHERE id = :id", nativeQuery = true)
    int cambiarRol(@Param("id") Long id, @Param("rol") String rol);

    /** Crea la fila en "empleado" si el usuario pasa a ser Empleado o Manager. */
    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO empleado (id) VALUES (:id) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    void asegurarDatosDeEmpleado(@Param("id") Long id);
}
