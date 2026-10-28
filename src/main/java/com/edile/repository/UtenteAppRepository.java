package com.edile.repository;

import com.edile.entity.UtenteApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteAppRepository extends JpaRepository<UtenteApp, Long> {
    Optional<UtenteApp> findByUsernameIgnoreCase(String username);
}
