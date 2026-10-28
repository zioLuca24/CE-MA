package com.edile.repository;

import com.edile.entity.Fornitore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {
    Optional<Fornitore> findByNomeIgnoreCase(String nome);
    List<Fornitore> findAllByOrderByNomeAsc();
}
