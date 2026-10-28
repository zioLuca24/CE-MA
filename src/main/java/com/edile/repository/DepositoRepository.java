package com.edile.repository;

import com.edile.entity.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    List<Deposito> findByCategoriaIgnoreCase(String categoria);
    List<Deposito> findByNomeArticoloContainingIgnoreCase(String nome);
    Deposito findByCodiceArticoloIgnoreCase(String codiceArticolo);
}
