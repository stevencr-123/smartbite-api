package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.CodigoQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoQRRepository extends JpaRepository<CodigoQR, Long> {
}

