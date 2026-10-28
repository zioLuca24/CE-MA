package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UTENTI_APP")
@Data
@NoArgsConstructor
public class UtenteApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "utenti_app_seq")
    @SequenceGenerator(name = "utenti_app_seq", sequenceName = "SEQ_UTENTI_APP", allocationSize = 1)
    @Column(name = "UTENTE_ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "NOME_COMPLETO", length = 255)
    private String nomeCompleto;

    @Column(name = "RUOLO", length = 50)
    private String ruolo;

    @Column(name = "ATTIVO")
    private Boolean attivo;

    @Column(name = "DATA_CREAZIONE")
    private LocalDateTime dataCreazione;

    @Column(name = "ULTIMO_ACCESSO")
    private LocalDateTime ultimoAccesso;
}
