package com.smartbite.operativo.repository;

import com.smartbite.operativo.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findBySucursalId(Long sucursalId);
}

