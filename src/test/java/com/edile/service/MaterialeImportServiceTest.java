package com.edile.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialeImportServiceTest {

    private final MaterialeImportService service = new MaterialeImportService();

    @Test
    void parsesLegacyDelimitedRows() {
        String text = "BOLLA-1;SABBIA FINE;12,50;0;3,40;Cantiere Nord";

        List<MaterialeImportService.BollaRow> rows = service.parseRowsFromText(text);

        assertEquals(1, rows.size());
        assertEquals("SABBIA FINE", rows.get(0).getNomeMateriale());
        assertEquals(12.5, rows.get(0).getQuantita(), 0.001);
        assertEquals(3.4, rows.get(0).getPrezzoUnitario(), 0.001);
    }

    @Test
    void parsesSupplierLayoutRowsIncludingWrappedDescription() {
        String text = ""
                + "PARTITA/DOC NR. 11060191217\n"
                + "Destinazione\n"
                + "lago patria\n"
                + "CODICE ARTICOLO DESCRIZIONE U.M QUANTITA' PREZZO IMPORTO RIGA C.IVA\n"
                + "I25DS400 IC 400 GRIGIO KG 25 PZ 120,00 1,97 236,68 22\n"
                + "PREMAR BANCALE PREMAR PZ 4,00 5,05 20,20 22\n"
                + "35 GUIDA T X INTONACO ZINC.2.8 CF PZ\n"
                + "50 PZ 150,00 0,48 71,34 22\n"
                + "102684 STADIA MURATORE ALL.H MT.3 PZ 2,00 30,33 60,66 22\n";

        List<MaterialeImportService.BollaRow> rows = service.parseRowsFromText(text);

        assertEquals(4, rows.size());
        assertEquals("11060191217", rows.get(0).getNumeroBolla());
        assertEquals("I25DS400", rows.get(0).getCodiceArticolo());
        assertEquals("IC 400 GRIGIO KG 25", rows.get(0).getDescrizione());
        assertEquals(236.68, rows.get(0).getPrezzoTotale(), 0.001);

        assertEquals("35", rows.get(2).getCodiceArticolo());
        assertEquals("GUIDA T X INTONACO ZINC.2.8 CF PZ 50", rows.get(2).getDescrizione());
        assertEquals(150.0, rows.get(2).getQuantita(), 0.001);
        assertEquals("lago patria", rows.get(2).getCantiereRaw());
    }
}
