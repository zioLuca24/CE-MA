package com.edile.repository;

import com.edile.entity.MovimentoMateriale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentoMaterialeRepository extends JpaRepository<MovimentoMateriale, Long> {
    List<MovimentoMateriale> findByCantiere_Id(Long cantiereId);
    List<MovimentoMateriale> findByMateriale_Id(Long materialeId);
    List<MovimentoMateriale> findByCantiere_IdAndMateriale_Id(Long cantiereId, Long materialeId);
    List<MovimentoMateriale> findByCantiere_IdAndTipoMovimento(Long cantiereId, String tipoMovimento);
    List<MovimentoMateriale> findByAttivita_IdOrderByDataMovimentoDescIdDesc(Long attivitaId);
    List<MovimentoMateriale> findByDataMovimentoBetween(LocalDate dal, LocalDate al);
    List<MovimentoMateriale> findByCantiere_IdAndDataMovimentoBetween(Long cantiereId, LocalDate dal, LocalDate al);
}
