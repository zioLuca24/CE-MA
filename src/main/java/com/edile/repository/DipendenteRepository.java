package com.edile.repository;

import com.edile.entity.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DipendenteRepository extends JpaRepository<Dipendente, Long> {
    List<Dipendente> findByStato(String stato);
    List<Dipendente> findByCognomeContainingIgnoreCase(String cognome);
}
