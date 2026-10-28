package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "ATTIVITA_MATERIALI_UTILIZZO")
@Data
@NoArgsConstructor
public class AttivitaMaterialeUtilizzo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attivita_materiali_utilizzo_seq")
    @SequenceGenerator(name = "attivita_materiali_utilizzo_seq", sequenceName = "SEQ_ATTIVITA_MATERIALI_UTILIZZO", allocationSize = 1)
    @Column(name = "UTILIZZO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ATTIVITA_ID", nullable = false)
    private AttivitaCantiere attivita;

    @ManyToOne
    @JoinColumn(name = "MATERIALE_ID", nullable = false)
    private MaterialeCatalogo materiale;

    @Column(name = "QUANTITA", precision = 15, scale = 4)
    private Double quantita;

    @Column(name = "SCARICATO")
    private Boolean scaricato;
}
