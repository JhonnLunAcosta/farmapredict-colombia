package com.farmapredict.repository;

import com.farmapredict.model.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByCodigo(String codigo);

    @Query("SELECT m FROM Medicamento m WHERE "
         + "(:q = '' OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :q, '%')) "
         + "OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :q, '%')) "
         + "OR (m.principioActivo IS NOT NULL AND LOWER(m.principioActivo) LIKE LOWER(CONCAT('%', :q, '%'))) "
         + "OR (m.titular IS NOT NULL AND LOWER(m.titular) LIKE LOWER(CONCAT('%', :q, '%')))) "
         + "AND (:estado IS NULL OR m.estadoRegistro = :estado)")
    Page<Medicamento> buscar(@Param("q") String q, @Param("estado") String estado, Pageable pageable);
}
