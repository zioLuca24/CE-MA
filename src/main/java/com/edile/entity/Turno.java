package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "TURNI")
@Data
@NoArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "turni_seq")
    @SequenceGenerator(name = "turni_seq", sequenceName = "SEQ_TURNI", allocationSize = 1)
    @Column(name = "TURNO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "DIPENDENTE_ID", nullable = false)
    private Dipendente dipendente;

    @Column(name = "DATA")
    private LocalDate data;

    @Column(name = "ORA_INIZIO")
    private LocalTime oraInizio;

    @Column(name = "ORA_FINE")
    private LocalTime oraFine;

    @Column(name = "NOTE", length = 500)
    private String note;

    @Column(name = "STATO_ATTIVITA", length = 50)
    private String statoAttivita;

}
