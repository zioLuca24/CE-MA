package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "MOVIMENTI_DEPOSITO")
@Data
@NoArgsConstructor
public class MovimentoDeposito {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movimento_deposito_seq")
    @SequenceGenerator(name = "movimento_deposito_seq", sequenceName = "SEQ_MOVIMENTI_DEPOSITO", allocationSize = 1)
    @Column(name = "MOVIMENTO_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "DEPOSITO_ID", nullable = false)
    private Deposito deposito;

    @ManyToOne
    @JoinColumn(name = "CANTIERE_ID")
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "DIPENDENTE_ID")
    private Dipendente dipendente;

    @Column(name = "TIPO_MOVIMENTO", nullable = false, length = 20)
    private String tipoMovimento;

    @Column(name = "QUANTITA", precision = 15, scale = 4)
    private Double quantita;

    @Column(name = "DATA_MOVIMENTO")
    private LocalDate dataMovimento;

    @Column(name = "NOTE", length = 500)
    private String note;
}
