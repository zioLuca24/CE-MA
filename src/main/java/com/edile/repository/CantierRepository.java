package com.edile.repository;

import com.edile.entity.Cantiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CantierRepository extends JpaRepository<Cantiere, Long> {
    List<Cantiere> findByStato(String stato);
    List<Cantiere> findByNomeContainingIgnoreCase(String nome);
}
