package com.edile.repository;

import com.edile.entity.DettaglioBolla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DettaglioBollaRepository extends JpaRepository<DettaglioBolla, Long> {
    List<DettaglioBolla> findByBolla_Id(Long bollaId);
}
