package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByOrdenId(Long ordenId);

    List<Pago> findByOrdenIdAndEstado(Long ordenId, EstadoPago estado);
}

