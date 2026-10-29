package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "BOLLE_MATERIALI_IMPORT")
@Data
@NoArgsConstructor
public class BollaMaterialeImport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bolle_materiali_import_seq")
    @SequenceGenerator(name = "bolle_materiali_import_seq", sequenceName = "SEQ_BOLLE_MATERIALI_IMPORT", allocationSize = 1)
    @Column(name = "IMPORT_ID")
    private Long id;

    @Column(name = "NUMERO_BOLLA", nullable = false, length = 80)
    private String numeroBolla;

    @Column(name = "CODICE_ARTICOLO", length = 80)
    private String codiceArticolo;

    @Column(name = "DESCRIZIONE_ARTICOLO", length = 500)
    private String descrizione;

    @Column(name = "UNITA_MISURA", length = 20)
    private String unitaMisura;

    @Column(name = "SCONTI", length = 50)
    private String sconti;

    @Column(name = "CODICE_IVA", length = 10)
    private String codiceIva;

    @ManyToOne
    @JoinColumn(name = "MATERIALE_ID", nullable = false)
    private MaterialeCatalogo materiale;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID", nullable = false)
    private Cantiere cantiere;

    @Column(name = "QUANTITA", precision = 15, scale = 4)
    private Double quantita;

    @Column(name = "PESO", precision = 15, scale = 4)
    private Double peso;

    @Column(name = "PREZZO_UNITARIO", precision = 15, scale = 4)
    private Double prezzoUnitario;

    @Column(name = "PREZZO_TOTALE", precision = 15, scale = 4)
    private Double prezzoTotale;

    @Column(name = "FORNITORE_NOME", length = 255)
    private String fornitoreNome;

    @Column(name = "DATA_IMPORT")
    private LocalDate dataImport;

    @Column(name = "FILE_ORIGINE", length = 255)
    private String fileOrigine;
}
