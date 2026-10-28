package com.edile.repository;

import com.edile.entity.MovimentoDeposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoDepositoRepository extends JpaRepository<MovimentoDeposito, Long> {
    List<MovimentoDeposito> findByDeposito_Id(Long depositoId);
    List<MovimentoDeposito> findByCantiere_Id(Long cantiereId);
    List<MovimentoDeposito> findByTipoMovimento(String tipoMovimento);
}
