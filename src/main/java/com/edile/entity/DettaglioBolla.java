package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "DETTAGLI_BOLLA")
@Data
@NoArgsConstructor
public class DettaglioBolla {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dettagli_seq")
    @SequenceGenerator(name = "dettagli_seq", sequenceName = "SEQ_DETTAGLI_BOLLA", allocationSize = 1)
    @Column(name = "DETTAGLIO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "BOLLA_ID", nullable = false)
    private BollaAcquisto bolla;

    @ManyToOne
    @JoinColumn(name = "MATERIALE_ID", nullable = false)
    private MaterialeCatalogo materiale;

    @Column(name = "QUANTITA", precision = 15, scale = 4)
    private Double quantita;

    @Column(name = "PREZZO_UNITARIO", precision = 15, scale = 4)
    private Double prezzoUnitario;

    @Column(name = "IMPORTO", precision = 15, scale = 2)
    private Double importo;

}
