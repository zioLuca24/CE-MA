package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "MOVIMENTI_MATERIALI")
@Data
@NoArgsConstructor
public class MovimentoMateriale {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movimenti_materiali_seq")
    @SequenceGenerator(name = "movimenti_materiali_seq", sequenceName = "SEQ_MOVIMENTI_MATERIALI", allocationSize = 1)
    @Column(name = "MOVIMENTO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MATERIALE_ID", nullable = false)
    private MaterialeCatalogo materiale;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "ATTIVITA_ID")
    private AttivitaCantiere attivita;

    @Column(name = "TIPO_MOVIMENTO", length = 20)
    private String tipoMovimento;

    @Column(name = "QUANTITA", precision = 15, scale = 4)
    private Double quantita;

    @Column(name = "DATA_MOVIMENTO")
    private LocalDate dataMovimento;

    @Column(name = "NOTE", length = 500)
    private String note;
}
