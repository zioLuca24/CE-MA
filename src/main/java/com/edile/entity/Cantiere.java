package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CANTIERI")
@Data
@NoArgsConstructor
public class Cantiere {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cantieri_seq")
    @SequenceGenerator(name = "cantieri_seq", sequenceName = "SEQ_CANTIERI", allocationSize = 1)
    @Column(name = "CANTIERE_ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 255)
    private String nome;

    @Column(name = "INDIRIZZO", length = 500)
    private String indirizzo;

    @Column(name = "DATA_INIZIO")
    private LocalDate dataInizio;

    @Column(name = "DATA_FINE")
    private LocalDate dataFine;

    @Column(name = "RESPONSABILE", length = 255)
    private String responsabile;

    @Column(name = "BUDGET", precision = 15, scale = 2)
    private Double budget;

    @Column(name = "STATO", length = 50)
    private String stato;

}
