package com.edile.repository;

import com.edile.entity.SogliaMateriale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SogliaMaterialeRepository extends JpaRepository<SogliaMateriale, Long> {
    List<SogliaMateriale> findByAttivaTrue();
    List<SogliaMateriale> findByCantiere_Id(Long cantiereId);
}
