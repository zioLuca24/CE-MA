package com.edile.repository;

import com.edile.entity.AssegnazioneCantiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssegnazioneCantiereRepository extends JpaRepository<AssegnazioneCantiere, Long> {
    List<AssegnazioneCantiere> findByCantiere_Id(Long cantiereId);
    List<AssegnazioneCantiere> findByDipendente_Id(Long dipendenteId);
    List<AssegnazioneCantiere> findByStato(String stato);
}
