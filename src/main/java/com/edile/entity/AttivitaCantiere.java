package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ATTIVITA_CANTIERE")
@Data
@NoArgsConstructor
public class AttivitaCantiere {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attivita_cantiere_seq")
    @SequenceGenerator(name = "attivita_cantiere_seq", sequenceName = "SEQ_ATTIVITA_CANTIERE", allocationSize = 1)
    @Column(name = "ATTIVITA_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @Column(name = "TITOLO", nullable = false, length = 255)
    private String titolo;

    @Column(name = "DESCRIZIONE", length = 1000)
    private String descrizione;

    @Column(name = "STATO", length = 50)
    private String stato;

    @Column(name = "DATA_PIANIFICATA")
    private LocalDate dataPianificata;

    @Column(name = "DATA_COMPLETAMENTO")
    private LocalDate dataCompletamento;

    @Column(name = "COSTO_MANODOPERA", precision = 15, scale = 2)
    private Double costoManodopera;
}
