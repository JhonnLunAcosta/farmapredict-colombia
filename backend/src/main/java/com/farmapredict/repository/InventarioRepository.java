package com.farmapredict.repository;

import com.farmapredict.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findAll();
}
