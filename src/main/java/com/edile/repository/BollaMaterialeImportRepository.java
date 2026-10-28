package com.edile.repository;

import com.edile.entity.BollaMaterialeImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BollaMaterialeImportRepository extends JpaRepository<BollaMaterialeImport, Long> {
    List<BollaMaterialeImport> findByNumeroBolla(String numeroBolla);
    List<BollaMaterialeImport> findByCantiere_Id(Long cantiereId);
}
