package com.edile.repository;

import com.edile.entity.AttivitaMaterialeUtilizzo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttivitaMaterialeUtilizzoRepository extends JpaRepository<AttivitaMaterialeUtilizzo, Long> {
    List<AttivitaMaterialeUtilizzo> findByAttivita_Id(Long attivitaId);
    List<AttivitaMaterialeUtilizzo> findByAttivita_IdAndScaricatoFalse(Long attivitaId);
    List<AttivitaMaterialeUtilizzo> findByAttivita_Cantiere_Id(Long cantiereId);
}
