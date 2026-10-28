package com.edile.repository;

import com.edile.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByCantiere_Id(Long cantiereId);
    List<Turno> findByDipendente_Id(Long dipendenteId);
    List<Turno> findByCantiere_IdAndDataBetween(Long cantiereId, LocalDate dataInizio, LocalDate dataFine);
}
