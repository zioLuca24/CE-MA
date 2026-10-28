package com.edile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "FORNITORI")
@Data
@NoArgsConstructor
public class Fornitore {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fornitori_seq")
    @SequenceGenerator(name = "fornitori_seq", sequenceName = "SEQ_FORNITORI", allocationSize = 1)
    @Column(name = "FORNITORE_ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 255)
    private String nome;

    @Column(name = "INDIRIZZO", length = 500)
    private String indirizzo;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "PARTITA_IVA", length = 20)
    private String partitaIva;

}
