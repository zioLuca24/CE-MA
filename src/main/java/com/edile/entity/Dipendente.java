package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "DIPENDENTI")
@Data
@NoArgsConstructor
public class Dipendente {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dipendenti_seq")
    @SequenceGenerator(name = "dipendenti_seq", sequenceName = "SEQ_DIPENDENTI", allocationSize = 1)
    @Column(name = "DIPENDENTE_ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 255)
    private String nome;

    @Column(name = "COGNOME", nullable = false, length = 255)
    private String cognome;

    @Column(name = "QUALIFICA", length = 100)
    private String qualifica;

    @Column(name = "DATA_ASSUNZIONE")
    private LocalDate dataAssunzione;

    @Column(name = "STATO", length = 50)
    private String stato;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "EMAIL", length = 255)
    private String email;

}
