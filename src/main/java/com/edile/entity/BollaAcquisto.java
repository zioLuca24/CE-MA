package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "BOLLE_ACQUISTO")
@Data
@NoArgsConstructor
public class BollaAcquisto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bolle_seq")
    @SequenceGenerator(name = "bolle_seq", sequenceName = "SEQ_BOLLE_ACQUISTO", allocationSize = 1)
    @Column(name = "BOLLA_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "FORNITORE_ID", nullable = false)
    private Fornitore fornitore;

    @Column(name = "DATA")
    private LocalDate data;

    @Column(name = "NUMERO_BOLLA", length = 50)
    private String numeroBolla;

    @Column(name = "DATA_RICEVIMENTO")
    private LocalDate dataRicevimento;

    @Column(name = "IMPORTO_TOTALE", precision = 15, scale = 2)
    private Double importoTotale;

    @Column(name = "STATO", length = 50)
    private String stato;

}
