package com.farmapredict.repository;

import com.farmapredict.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByCodigo(String codigo);
}
