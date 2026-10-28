package com.edile.repository;

import com.edile.entity.BollaAcquisto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BollaAcquistoRepository extends JpaRepository<BollaAcquisto, Long> {
    List<BollaAcquisto> findByCantiere_Id(Long cantiereId);
    List<BollaAcquisto> findByStato(String stato);
    List<BollaAcquisto> findByDataBetween(LocalDate dataInizio, LocalDate dataFine);
}
