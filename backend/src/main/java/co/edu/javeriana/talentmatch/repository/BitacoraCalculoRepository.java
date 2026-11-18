package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.BitacoraCalculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * Bitacora del calculo (RNF-06). A proposito NO extiende JpaRepository:
 * solo expone insertar y consultar. No existe ningun metodo para modificar
 * o borrar registros (y la base de datos tambien lo impide con un trigger).
 */
public interface BitacoraCalculoRepository extends Repository<BitacoraCalculo, Long> {

    BitacoraCalculo save(BitacoraCalculo registro);

    Optional<BitacoraCalculo> findById(Long id);

    List<BitacoraCalculo> findByEmpleadoIdAndVacanteIdOrderByFechaHoraDesc(Long empleadoId, Long vacanteId);

    long count();
}
