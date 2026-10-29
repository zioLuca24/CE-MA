package com.edile.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BollaTableParserTest {

    private static final double ROW_SLOPE = -0.026;
    private static final double HEADER_SLOPE = -0.0286;

    private final List<OcrWord> words = new ArrayList<>();

    private void word(String text, int x, int w, double baseY, double slope) {
        int y = (int) Math.round(baseY + slope * (x - 367));
        words.add(new OcrWord(text, x, y, w, 14));
    }

    private void row(double base, String code, int codeW, String[] desc, int[] descX, int[] descW,
                     String qty, String price, String total, String iva) {
        word(code, 358, codeW, base + 6, ROW_SLOPE);
        for (int i = 0; i < desc.length; i++) {
            word(desc[i], descX[i], descW[i], base, ROW_SLOPE);
        }
        word("PZ", 845, 25, base, ROW_SLOPE);
        word(qty, 1015 - qty.length() * 9, qty.length() * 9, base - 4, ROW_SLOPE);
        word(price, 1155 - price.length() * 9, price.length() * 9, base - 4, ROW_SLOPE);
        word(total, 1445 - total.length() * 9, total.length() * 9, base - 4, ROW_SLOPE);
        word(iva, 1530, 22, base - 12, ROW_SLOPE);
    }

    private void fotoInclinata() {
        word("CODICE", 367, 45, 190, HEADER_SLOPE);
        word("ARTICOLO", 415, 62, 190, HEADER_SLOPE);
        word("DESCRIZIONE", 625, 80, 190, HEADER_SLOPE);
        word("U.M", 851, 22, 190, HEADER_SLOPE);
        word("QUANTITA'", 920, 65, 190, HEADER_SLOPE);
        word("PREZZO", 1070, 50, 190, HEADER_SLOPE);
        word("SCONTI", 1212, 46, 190, HEADER_SLOPE);
        word("IMPORTO", 1333, 50, 190, HEADER_SLOPE);
        word("RIGA", 1385, 30, 190, HEADER_SLOPE);
        word("C.IVA", 1488, 35, 190, HEADER_SLOPE);
        word("|", 485, 4, 200, 0);
        word("|", 840, 4, 230, 0);

        row(214, "I25DS400", 70, new String[] {"IC", "400", "GRIGIO", "KG", "25"},
                new int[] {495, 520, 560, 625, 655}, new int[] {20, 35, 60, 25, 22}, "120,00", "1,97", "236,68", "22");
        row(239, "PREMAR", 60, new String[] {"BANCALE", "PREMAR"},
                new int[] {495, 570}, new int[] {70, 60}, "4,00", "5,05", "20,20", "22");
        row(264, "35", 20, new String[] {"GUIDA", "A", "T", "X", "INTONACO", "ZINC.2,8", "CF", "PZ"},
                new int[] {495, 545, 562, 578, 595, 680, 745, 770},
                new int[] {45, 12, 10, 12, 80, 60, 20, 22}, "150,00", "0,48", "71,34", "22");
        word("50", 495, 20, 284, ROW_SLOPE);
        row(306, "102684", 60, new String[] {"STADIA", "MURATORE", "ALL.H", "MT.3"},
                new int[] {495, 555, 640, 690}, new int[] {55, 80, 45, 35}, "2,00", "30,33", "60,66", "22");

        word("ATTENZIONE", 347, 100, 366, -0.02);
        word("LA", 460, 20, 366, -0.02);
        word("MERCE", 500, 50, 366, -0.02);
        word("NUMERO", 330, 60, 1165, 0);
        word("COLLI", 395, 50, 1165, 0);
        word("TOTALE", 1130, 60, 1165, 0);
        word("DOCUMENTO", 1190, 80, 1165, 0);
        word("0,00", 340, 40, 1195, 0);
        word("0,00", 680, 40, 1195, 0);
        word("0,00", 980, 40, 1195, 0);
        word("474,43", 1210, 55, 1195, 0);
        word("388,88", 1050, 55, 1280, 0);
        word("85,55", 1280, 45, 1280, 0);
        word("VIALE", 940, 50, 5, 0);
        word("EUROPA", 995, 60, 5, 0);
        word("160", 1060, 30, 5, 0);
    }

    @Test
    void readsAllRowsOfSkewedPhoto() {
        fotoInclinata();

        List<BollaTableParser.ParsedRow> rows = BollaTableParser.parse(words);

        assertEquals(4, rows.size());

        BollaTableParser.ParsedRow r1 = rows.get(0);
        assertEquals("I25DS400", r1.codice);
        assertEquals("IC 400 GRIGIO KG 25", r1.descrizione);
        assertEquals("PZ", r1.um);
        assertEquals(120.0, r1.quantita, 0.001);
        assertEquals(1.97, r1.prezzo, 0.001);
        assertEquals(236.68, r1.importo, 0.001);
        assertEquals("22", r1.iva);
        assertEquals("", r1.sconti);

        BollaTableParser.ParsedRow r2 = rows.get(1);
        assertEquals("PREMAR", r2.codice);
        assertEquals("BANCALE PREMAR", r2.descrizione);
        assertEquals(4.0, r2.quantita, 0.001);
        assertEquals(5.05, r2.prezzo, 0.001);
        assertEquals(20.20, r2.importo, 0.001);

        BollaTableParser.ParsedRow r3 = rows.get(2);
        assertEquals("35", r3.codice);
        assertEquals("GUIDA A T X INTONACO ZINC.2,8 CF PZ 50", r3.descrizione);
        assertEquals("PZ", r3.um);
        assertEquals(150.0, r3.quantita, 0.001);
        assertEquals(0.48, r3.prezzo, 0.001);
        assertEquals(71.34, r3.importo, 0.001);
        assertEquals("22", r3.iva);

        BollaTableParser.ParsedRow r4 = rows.get(3);
        assertEquals("102684", r4.codice);
        assertEquals("STADIA MURATORE ALL.H MT.3", r4.descrizione);
        assertEquals(2.0, r4.quantita, 0.001);
        assertEquals(30.33, r4.prezzo, 0.001);
        assertEquals(60.66, r4.importo, 0.001);
    }

    @Test
    void rebuildsReadableTextLineByLine() {
        fotoInclinata();

        String text = BollaTableParser.toText(words);

        assertTrue(text.contains("I25DS400"));
        assertTrue(text.contains("STADIA MURATORE"));
    }

    @Test
    void readsDiscountColumnWhenPresent() {
        fotoInclinata();
        word("10+5", 1212, 40, 210, ROW_SLOPE);

        List<BollaTableParser.ParsedRow> rows = BollaTableParser.parse(words);

        assertEquals(4, rows.size());
        assertEquals("10+5", rows.get(0).sconti);
        assertEquals(1.97, rows.get(0).prezzo, 0.001);
        assertEquals(236.68, rows.get(0).importo, 0.001);
    }
}
