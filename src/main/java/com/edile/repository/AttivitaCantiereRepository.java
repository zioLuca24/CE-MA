package com.edile.repository;

import com.edile.entity.AttivitaCantiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttivitaCantiereRepository extends JpaRepository<AttivitaCantiere, Long> {
    List<AttivitaCantiere> findByCantiere_Id(Long cantiereId);
}
