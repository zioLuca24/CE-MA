package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "SOGLIE_MATERIALI")
@Data
@NoArgsConstructor
public class SogliaMateriale {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "soglie_materiali_seq")
    @SequenceGenerator(name = "soglie_materiali_seq", sequenceName = "SEQ_SOGLIE_MATERIALI", allocationSize = 1)
    @Column(name = "SOGLIA_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MATERIALE_ID", nullable = false)
    private MaterialeCatalogo materiale;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID")
    private Cantiere cantiere;

    @Column(name = "GIACENZA_MINIMA", precision = 15, scale = 4)
    private Double giacenzaMinima;

    @Column(name = "GIACENZA_CRITICA", precision = 15, scale = 4)
    private Double giacenzaCritica;

    @Column(name = "CONSUMO_MENSILE_MASSIMO", precision = 15, scale = 4)
    private Double consumoMensileMassimo;

    @Column(name = "ATTIVA")
    private Boolean attiva;
}
