package com.farmapredict.repository;

import com.farmapredict.model.SismedPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SismedPrecioRepository extends JpaRepository<SismedPrecio, Long> {
    List<SismedPrecio> findByCodigoCumOrderByPeriodoDesc(String codigoCum);
    Optional<SismedPrecio> findByCodigoCumAndPeriodoAndCanal(String codigoCum, String periodo, String canal);
}
