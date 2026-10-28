package com.edile.repository;

import com.edile.entity.MaterialeCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialeCatalogoRepository extends JpaRepository<MaterialeCatalogo, Long> {
    List<MaterialeCatalogo> findByCategoriaIgnoreCase(String categoria);
    List<MaterialeCatalogo> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT m FROM MaterialeCatalogo m WHERE UPPER(m.nome) = UPPER(:nome)")
    List<MaterialeCatalogo> findByNomeIgnoreCaseList(@Param("nome") String nome);
}