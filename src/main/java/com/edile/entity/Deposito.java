package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "DEPOSITO_MATERIALI")
@Data
@NoArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deposito_seq")
    @SequenceGenerator(name = "deposito_seq", sequenceName = "SEQ_DEPOSITO_MATERIALI", allocationSize = 1)
    @Column(name = "DEPOSITO_ID")
    private Long id;

    @Column(name = "CODICE_ARTICOLO", length = 80)
    private String codiceArticolo;

    @Column(name = "NOME_ARTICOLO", nullable = false, length = 255)
    private String nomeArticolo;

    @Column(name = "DESCRIZIONE", length = 500)
    private String descrizione;

    @Column(name = "CATEGORIA", length = 100)
    private String categoria;

    @Column(name = "UNITA_MISURA", length = 20)
    private String unitaMisura;

    @Column(name = "QUANTITA_DISPONIBILE", precision = 15, scale = 4)
    private Double quantitaDisponibile;

    @Column(name = "QUANTITA_UTILIZZATA", precision = 15, scale = 4)
    private Double quantitaUtilizzata;

    @Column(name = "PREZZO_UNITARIO", precision = 15, scale = 4)
    private Double prezzoUnitario;

    @Column(name = "UBICAZIONE", length = 255)
    private String ubicazione;

    @Column(name = "NOTE", length = 500)
    private String note;
}
