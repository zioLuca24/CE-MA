package com.edile.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MaterialeImportService {

    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d+[\\.,]\\d+");
    private static final Pattern MONEY_PATTERN = Pattern.compile("\\d+[\\.,]\\d{2}");
    private static final Pattern UM_PATTERN = Pattern.compile("\\b(PZ|KG|LT|MT|M2|M3|NR)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROW_UM_PATTERN = Pattern.compile("\\b(PZ|KG|LT|MT|M2|M3)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARTICLE_CODE_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9\\-/]{0,19}$");

    public List<BollaRow> parseBollaRows(MultipartFile file) throws IOException {
        String text = extractText(file);
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Il file non contiene testo leggibile.");
        }
        List<BollaRow> righe = parseRowsFromText(text);
        if (righe.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nessuna riga valida trovata nel documento caricato."
            );
        }
        return righe;
    }

    List<BollaRow> parseRowsFromText(String text) {
        return parseRows(text);
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        if (filename.endsWith(".pdf") || contentType.contains("pdf")) {
            return extractPdfText(file);
        }
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png") || contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png")) {
            return extractImageText(file);
        }
        throw new IllegalArgumentException("Formato non supportato. Carica un PDF, JPG o PNG.");
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }
            return extractPdfImageText(document);
        }
    }

    private String extractPdfImageText(PDDocument document) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        StringBuilder text = new StringBuilder();
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, 200);
            String pageText = SimpleImageOcr.extractText(image);
            if (pageText != null && !pageText.trim().isEmpty()) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(pageText);
            }
        }
        return text.toString();
    }

    private String extractImageText(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Immagine JPG non valida.");
        }
        return SimpleImageOcr.extractText(image);
    }

    private List<BollaRow> parseRows(String text) {
        List<BollaRow> result = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        String fornitoreNome = ""; // Estrazione automatica fornitore disattivata: inserimento manuale da UI
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.toLowerCase(Locale.ROOT).startsWith("numero")) {
                continue;
            }
            BollaRow row = parseLine(line);
            if (row != null) {
                row.setFornitoreNome(fornitoreNome);
                result.add(row);
            }
        }

        if (!result.isEmpty()) {
            return result;
        }

        result = parseFixedLayoutRows(text);
        if (result.isEmpty()) {
            return result;
        }

        for (BollaRow row : result) {
            row.setFornitoreNome(fornitoreNome);
        }
        return result;
    }

    private BollaRow parseLine(String line) {
        String[] semicolon = line.split(";");
        if (semicolon.length >= 6) {
            return buildRow(semicolon);
        }

        String[] comma = line.split(",");
        if (comma.length >= 6) {
            return buildRow(comma);
        }

        String[] pipe = line.split("\\|");
        if (pipe.length >= 6) {
            return buildRow(pipe);
        }

        return null;
    }

    private BollaRow buildRow(String[] fields) {
        if (fields.length >= 7) {
            String numeroBolla = fields[0].trim();
            String codiceArticolo = fields[1].trim();
            String descrizione = fields[2].trim();
            Double quantita = parseDouble(fields[3]);
            Double prezzoUnitario = parseDouble(fields[4]);
            Double prezzoTotale = parseDouble(fields[5]);
            String cantiere = fields[6].trim();

            if (numeroBolla.isEmpty() || descrizione.isEmpty() || quantita == null) {
                return null;
            }

            return new BollaRow(
                    numeroBolla,
                    codiceArticolo,
                    descrizione,
                    quantita,
                    0.0,
                    prezzoUnitario == null ? 0.0 : prezzoUnitario,
                    prezzoTotale == null ? 0.0 : prezzoTotale,
                    cantiere
            );
        }

        String numeroBolla = fields[0].trim();
        String nomeMateriale = fields[1].trim();
        Double quantita = parseDouble(fields[2]);
        Double peso = parseDouble(fields[3]);
        Double prezzoUnitario = parseDouble(fields[4]);
        String cantiere = fields[5].trim();

        if (numeroBolla.isEmpty() || nomeMateriale.isEmpty() || quantita == null) {
            return null;
        }

        return new BollaRow(numeroBolla, nomeMateriale, quantita, peso == null ? 0.0 : peso, prezzoUnitario == null ? 0.0 : prezzoUnitario, cantiere);
    }

    private List<BollaRow> parseFixedLayoutRows(String text) {
        List<BollaRow> result = new ArrayList<>();
        String numeroBolla = estraiNumeroBolla(text);
        String cantiereRaw = estraiDestinazione(text);
        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = normalizeSpaces(lines[i]);
            if (!isPotentialSupplierRowStart(line)) {
                continue;
            }

            String mergedLine = line;
            int nextIndex = i + 1;
            while (countMoneyValues(mergedLine) < 3 && nextIndex < lines.length) {
                String nextLine = normalizeSpaces(lines[nextIndex]);
                if (nextLine.isEmpty() || isSupplierSectionFooter(nextLine)) {
                    break;
                }
                if (isPotentialSupplierRowStart(nextLine) && countMoneyValues(mergedLine) > 0) {
                    break;
                }
                mergedLine = mergedLine + " " + nextLine;
                nextIndex++;
            }

            BollaRow row = parseSupplierRow(mergedLine, numeroBolla, cantiereRaw);
            if (row != null) {
                result.add(row);
                i = nextIndex - 1;
            }
        }

        return result;
    }

    private BollaRow parseSupplierRow(String line, String numeroBolla, String cantiereRaw) {
        String normalized = normalizeSpaces(line);
        if (normalized.isEmpty()) {
            return null;
        }

        int firstSpace = normalized.indexOf(' ');
        if (firstSpace <= 0) {
            return null;
        }

        String codiceArticolo = normalized.substring(0, firstSpace).trim();
        if (!ARTICLE_CODE_PATTERN.matcher(codiceArticolo).matches() || isReservedSupplierWord(codiceArticolo)) {
            return null;
        }

        List<ValueMatch> valori = estraiValoriMonetari(normalized);
        if (valori.size() < 3) {
            return null;
        }

        ValueMatch quantitaToken = valori.get(0);
        ValueMatch prezzoUnitarioToken = valori.get(1);
        ValueMatch prezzoTotaleToken = valori.get(2);

        String descrizioneConUm = normalized.substring(firstSpace, quantitaToken.start).trim();
        String descrizione = estraiDescrizioneDaPrefisso(descrizioneConUm);
        if (descrizione.isEmpty()) {
            return null;
        }

        Double quantita = parseDouble(quantitaToken.value);
        Double prezzoUnitario = parseDouble(prezzoUnitarioToken.value);
        Double prezzoTotale = parseDouble(prezzoTotaleToken.value);
        if (quantita == null || quantita <= 0.0) {
            return null;
        }

        return new BollaRow(
                numeroBolla,
                codiceArticolo,
                descrizione,
                quantita,
                0.0,
                prezzoUnitario == null ? 0.0 : prezzoUnitario,
                prezzoTotale == null ? 0.0 : prezzoTotale,
                cantiereRaw
        );
    }

    private String estraiDescrizioneDaPrefisso(String text) {
        String normalized = normalizeSpaces(text);
        if (normalized.isEmpty()) {
            return "";
        }

        Matcher matcher = UM_PATTERN.matcher(normalized);
        int lastStart = -1;
        while (matcher.find()) {
            lastStart = matcher.start();
        }

        String descrizione = lastStart >= 0 ? normalized.substring(0, lastStart).trim() : normalized;
        descrizione = descrizione.replaceAll("\\s+", " ").trim();
        return containsLettersOrDigits(descrizione) ? descrizione : "";
    }

    private boolean isPotentialSupplierRowStart(String line) {
        String normalized = normalizeSpaces(line);
        if (normalized.isEmpty() || normalized.length() < 6 || isSupplierSectionFooter(normalized)) {
            return false;
        }

        int firstSpace = normalized.indexOf(' ');
        if (firstSpace <= 0) {
            return false;
        }

        String firstToken = normalized.substring(0, firstSpace).trim();
        if (!ARTICLE_CODE_PATTERN.matcher(firstToken).matches() || isReservedSupplierWord(firstToken)) {
            return false;
        }

        String rest = normalized.substring(firstSpace + 1).trim();
        if (rest.isEmpty()) {
            return false;
        }

        int moneyCount = countMoneyValues(normalized);
        if (moneyCount == 0) {
            return rest.split(" ").length >= 2 && ROW_UM_PATTERN.matcher(rest).find();
        }

        int firstMoneyStart = firstMoneyStart(normalized);
        if (firstMoneyStart < 0) {
            return false;
        }

        String prefix = normalized.substring(firstSpace + 1, firstMoneyStart).trim();
        return containsLettersOrDigits(prefix) && !UM_PATTERN.matcher(prefix).matches();
    }

    private boolean isSupplierSectionFooter(String line) {
        String upper = line.toUpperCase(Locale.ROOT);
        return upper.startsWith("ATTENZIONE")
                || upper.startsWith("MERCE RITIRATA")
                || upper.startsWith("NUMERO COLLI")
                || upper.startsWith("TOTALE DOCUMENTO")
                || upper.startsWith("C. IVA")
                || upper.startsWith("DESCRIZIONE COD. IVA")
                || upper.startsWith("TRASPORTO A CURA");
    }

    private boolean isReservedSupplierWord(String token) {
        String upper = token.toUpperCase(Locale.ROOT);
        return "CODICE".equals(upper)
                || "DESCRIZIONE".equals(upper)
                || "QUANTITA".equals(upper)
                || "PREZZO".equals(upper)
                || "DESTINAZIONE".equals(upper)
                || "PAGINA".equals(upper)
                || "FOGLIO".equals(upper);
    }

    private int countMoneyValues(String text) {
        return estraiValoriMonetari(text).size();
    }

    private int firstMoneyStart(String text) {
        Matcher matcher = MONEY_PATTERN.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    private List<ValueMatch> estraiValoriMonetari(String text) {
        List<ValueMatch> values = new ArrayList<>();
        Matcher matcher = MONEY_PATTERN.matcher(text);
        while (matcher.find()) {
            values.add(new ValueMatch(matcher.group(), matcher.start()));
        }
        return values;
    }

    private String normalizeSpaces(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }

    private boolean containsLettersOrDigits(String text) {
        return text != null && text.matches(".*[A-Za-z0-9].*");
    }

    private List<Double> estraiDecimali(String text) {
        List<Double> values = new ArrayList<>();
        Matcher matcher = DECIMAL_PATTERN.matcher(text);
        while (matcher.find()) {
            Double value = parseDouble(matcher.group());
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private String normalizzaDescrizioneMateriale(String beforeUm) {
        String normalized = beforeUm.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }

        String[] parts = normalized.split(" ");
        if (parts.length >= 2 && parts[0].matches("[A-Za-z0-9]{6,}")) {
            String reduced = normalized.substring(parts[0].length()).trim();
            if (!reduced.isEmpty()) {
                return reduced;
            }
        }
        return normalized;
    }

    private String estraiNumeroBolla(String text) {
        String fallback = "BOLLA-SCONOSCIUTA";
        if (text == null || text.trim().isEmpty()) {
            return fallback;
        }

        String[] patterns = new String[] {
                "(?i)\\bNUMERO\\s*[:#-]?\\s*([A-Z0-9\\-/]{3,})",
                "(?i)\\bDDT\\s*[:#-]?\\s*([A-Z0-9\\-/]{3,})",
                "(?i)\\bDOCUMENTO\\s*[:#-]?\\s*([A-Z0-9\\-/]{3,})",
                "(?i)\\bPARTITA[/\\s]*DOC\\.?\\s*NR\\.?\\s*[:#-]?\\s*([A-Z0-9\\-/]{3,})"
        };

        for (String p : patterns) {
            Matcher m = Pattern.compile(p).matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        }

        return fallback;
    }

    private String estraiDestinazione(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i].trim();
            if (line.equalsIgnoreCase("destinazione") || line.toLowerCase(Locale.ROOT).startsWith("destinazione")) {
                for (int j = i + 1; j < Math.min(lines.length, i + 4); j++) {
                    String candidate = lines[j] == null ? "" : lines[j].trim();
                    if (!candidate.isEmpty()) {
                        return candidate;
                    }
                }
            }
        }

        return "";
    }

    /*
    private String estraiFornitore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] patterns = new String[] {
                "(?i)\\bFORNITORE\\s*[:\\-]?\\s*([A-Z0-9ÃƒÆ’Ã¢â€šÂ¬-ÃƒÆ’Ã¢â‚¬â€œÃƒÆ’Ã‹Å“-ÃƒÆ’Ã…Â¾&/.,' -]{3,})",
                "(?i)\\bDITTA\\s*[:\\-]?\\s*([A-Z0-9ÃƒÆ’Ã¢â€šÂ¬-ÃƒÆ’Ã¢â‚¬â€œÃƒÆ’Ã‹Å“-ÃƒÆ’Ã…Â¾&/.,' -]{3,})",
                "(?i)\\bVENDITORE\\s*[:\\-]?\\s*([A-Z0-9ÃƒÆ’Ã¢â€šÂ¬-ÃƒÆ’Ã¢â‚¬â€œÃƒÆ’Ã‹Å“-ÃƒÆ’Ã…Â¾&/.,' -]{3,})",
                "(?i)\\bFORNITORE\\s*([A-Z0-9ÃƒÆ’Ã¢â€šÂ¬-ÃƒÆ’Ã¢â‚¬â€œÃƒÆ’Ã‹Å“-ÃƒÆ’Ã…Â¾&/.,' -]{3,})\\s*(?:C\\.F|P\\.IVA|PARTITA IVA|C\\.F\\.|P\\.IVA\\.)"
        };

        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            if (matcher.find()) {
                String candidate = matcher.group(1).trim();
                candidate = candidate.replaceAll("\\s+", " ");
                if (!candidate.isEmpty() && !candidate.equalsIgnoreCase("ditta") && !candidate.equalsIgnoreCase("fornitore")) {
                    return candidate;
                }
            }
        }

        String[] lines = text.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = normalizeSpaces(rawLine);
            if (line.isEmpty()) {
                continue;
            }
            String upper = line.toUpperCase(Locale.ROOT);
            if (upper.contains("FORNITORE") || upper.contains("DITTA") || upper.contains("VENDITORE")) {
                String after = line.replaceFirst("(?i).*?(FORNITORE|DITTA|VENDITORE)\\s*[:\\-]?\\s*", "").trim();
                if (!after.isEmpty() && after.length() > 2) {
                    return after.split("\\s+(?=(?:C\\.|P\\.|PARTITA|CF|IVA|P\\.IVA|CAP))")[0].trim();
                }
            }
        }

        return "";
    }
    */

    private Double parseDouble(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬", "").replace(" ", "");
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(",", ".");
        }
        try {
            return Double.valueOf(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static class BollaRow {
        private String numeroBolla;
        private String codiceArticolo;
        private String descrizione;
        private Double quantita;
        private Double peso;
        private Double prezzoUnitario;
        private Double prezzoTotale;
        private String cantiereRaw;
        private String fornitoreNome;

        public BollaRow(String numeroBolla, String nomeMateriale, Double quantita, Double peso, Double prezzoUnitario, String cantiereRaw) {
            this(numeroBolla, "", nomeMateriale, quantita, peso, prezzoUnitario, calcolaPrezzoTotale(quantita, prezzoUnitario), cantiereRaw);
        }

        public BollaRow(String numeroBolla, String codiceArticolo, String descrizione, Double quantita, Double peso, Double prezzoUnitario, Double prezzoTotale, String cantiereRaw) {
            this.numeroBolla = numeroBolla;
            this.codiceArticolo = codiceArticolo;
            this.descrizione = descrizione;
            this.quantita = quantita;
            this.peso = peso;
            this.prezzoUnitario = prezzoUnitario;
            this.prezzoTotale = prezzoTotale;
            this.cantiereRaw = cantiereRaw;
            this.fornitoreNome = "";
        }

        private static Double calcolaPrezzoTotale(Double quantita, Double prezzoUnitario) {
            if (quantita == null || prezzoUnitario == null) {
                return 0.0;
            }
            return quantita * prezzoUnitario;
        }

        public String getNumeroBolla() {
            return numeroBolla;
        }

        public String getCodiceArticolo() {
            return codiceArticolo;
        }

        public String getDescrizione() {
            return descrizione;
        }

        public String getNomeMateriale() {
            return descrizione;
        }

        public Double getQuantita() {
            return quantita;
        }

        public Double getPeso() {
            return peso;
        }

        public Double getPrezzoUnitario() {
            return prezzoUnitario;
        }

        public Double getPrezzoTotale() {
            return prezzoTotale;
        }

        public String getCantiereRaw() {
            return cantiereRaw;
        }

        public String getFornitoreNome() {
            return fornitoreNome == null ? "" : fornitoreNome;
        }

        public void setFornitoreNome(String fornitoreNome) {
            this.fornitoreNome = fornitoreNome == null ? "" : fornitoreNome.trim();
        }
    }

    private static class ValueMatch {
        private final String value;
        private final int start;

        ValueMatch(String value, int start) {
            this.value = value;
            this.start = start;
        }
    }
}
