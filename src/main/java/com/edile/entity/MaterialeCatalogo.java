package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "CATALOGO_MATERIALI")
@Data
@NoArgsConstructor
public class MaterialeCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "materiali_seq")
    @SequenceGenerator(name = "materiali_seq", sequenceName = "SEQ_CATALOGO_MATERIALI", allocationSize = 1)
    @Column(name = "MATERIALE_ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 255)
    private String nome;

    @Column(name = "CATEGORIA", length = 100)
    private String categoria;

    @Column(name = "UNITA_MISURA", length = 20)
    private String unitaMisura;

    @Column(name = "PREZZO_MEDIO", precision = 15, scale = 4)
    private Double prezzoMedio;

    @Column(name = "DESCRIZIONE", length = 500)
    private String descrizione;

    @Column(name = "QUANTITA_ACQUISTATA_TOTALE", precision = 15, scale = 4)
    private Double quantitaAcquistataTotale;

    @Column(name = "QUANTITA_UTILIZZATA_TOTALE", precision = 15, scale = 4)
    private Double quantitaUtilizzataTotale;

    @Column(name = "QUANTITA_RESIDUA", precision = 15, scale = 4)
    private Double quantitaResidua;

}
