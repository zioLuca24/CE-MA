package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ASSEGNAZIONI_CANTIERE")
@Data
@NoArgsConstructor
public class AssegnazioneCantiere {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assegnazioni_seq")
    @SequenceGenerator(name = "assegnazioni_seq", sequenceName = "SEQ_ASSEGNAZIONI_CANTIERE", allocationSize = 1)
    @Column(name = "ASSEGNAZIONE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "DIPENDENTE_ID", nullable = false)
    private Dipendente dipendente;

    @Column(name = "RUOLO_CANTIERE", length = 100)
    private String ruoloCantiere;

    @Column(name = "DATA_INIZIO")
    private LocalDate dataInizio;

    @Column(name = "DATA_FINE")
    private LocalDate dataFine;

    @Column(name = "STATO", length = 50)
    private String stato;
}
