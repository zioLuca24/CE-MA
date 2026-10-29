package com.edile.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Ricostruisce le righe articolo di una bolla a partire dalle parole OCR con coordinate.
 * Non dipende dall'ordine di lettura di Tesseract: usa le intestazioni della tabella
 * (CODICE ARTICOLO, DESCRIZIONE, U.M., QUANTITA', PREZZO, SCONTI, IMPORTO RIGA, C.IVA)
 * per correggere l'inclinazione della foto e ancora ogni riga ai valori numerici.
 */
final class BollaTableParser {

    static final class ParsedRow {
        String codice = "";
        String descrizione = "";
        String um = "";
        Double quantita;
        Double prezzo;
        String sconti = "";
        Double importo;
        String iva = "";
    }

    private static final Pattern UM_PATTERN = Pattern.compile("^(PZ|P2|NR|KG|LT|MT|ML|MQ|M2|M3|CF|CT|SC|SA|BA|BN|PL|CONF|PAL|GR|TN|CAD)$");
    private static final Pattern MONEY_PATTERN = Pattern.compile("^(\\d{1,3}(\\.\\d{3})*,\\d{1,4}|\\d+[.,]\\d{1,4})$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9][0-9.,+%]*$");
    private static final Pattern IVA_PATTERN = Pattern.compile("^\\d{1,2}([.,]0+)?$");
    private static final String[] STOP_PREFIXES = new String[] {
            "ATTENZIONE", "SEGNALAZ", "RITIRAT", "COLLI", "IMPONIBILE", "ACCONTO", "OMAGGIO", "SPESE", "BOLLI", "TOTALE"
    };
    private static final String[] HEADER_KEYS = new String[] { "DESC", "QTA", "PREZZO", "SCONTI", "IMPORTO", "CIVA", "UM", "CODICE" };

    private BollaTableParser() {
    }

    private static final class Tok {
        final OcrWord word;
        final String text;
        final double cx;
        final double cy;
        double ys;

        Tok(OcrWord word, String text) {
            this.word = word;
            this.text = text;
            this.cx = word.centerX();
            this.cy = word.centerY();
            this.ys = this.cy;
        }
    }

    private static final class Header {
        final List<Tok> words = new ArrayList<>();
        double slope;
        double y;
        double codeRight = -1;
    }

    private static final class Analysis {
        List<Tok> tokens = new ArrayList<>();
        double medH;
        Header header;
    }

    static List<ParsedRow> parse(List<OcrWord> input) {
        List<ParsedRow> result = new ArrayList<>();
        Analysis analysis = analyze(input);
        if (analysis == null) {
            return result;
        }
        List<Tok> all = analysis.tokens;
        double medH = analysis.medH;
        Header header = analysis.header;

        double zoneTop = Double.NEGATIVE_INFINITY;
        double refY;
        if (header != null) {
            zoneTop = header.y + 0.9 * medH;
            refY = header.y + 2 * medH;
        } else {
            refY = Double.POSITIVE_INFINITY;
            for (Tok t : all) {
                if (isUm(t.text)) {
                    refY = Math.min(refY, t.ys);
                }
            }
            if (refY == Double.POSITIVE_INFINITY) {
                return result;
            }
        }

        double stopY = Double.POSITIVE_INFINITY;
        for (Tok t : all) {
            if (t.ys > refY && isStopWord(t.text)) {
                stopY = Math.min(stopY, t.ys);
            }
        }
        boolean stopFound = stopY != Double.POSITIVE_INFINITY;

        Set<Tok> headerSet = header == null ? new HashSet<Tok>() : new HashSet<>(header.words);
        List<Tok> zone = new ArrayList<>();
        for (Tok t : all) {
            if (t.ys > zoneTop && t.ys < stopY && !headerSet.contains(t)) {
                zone.add(t);
            }
        }

        List<Tok> seeds = new ArrayList<>();
        for (Tok t : zone) {
            if (isUm(t.text) || isMoney(t.text)) {
                seeds.add(t);
            }
        }
        sortByY(seeds);

        double tol = Math.max(0.75 * medH, 6);
        List<List<Tok>> clusters = new ArrayList<>();
        List<Double> sums = new ArrayList<>();
        for (Tok seed : seeds) {
            int last = clusters.size() - 1;
            if (last >= 0 && Math.abs(seed.ys - sums.get(last) / clusters.get(last).size()) <= tol) {
                clusters.get(last).add(seed);
                sums.set(last, sums.get(last) + seed.ys);
            } else {
                List<Tok> c = new ArrayList<>();
                c.add(seed);
                clusters.add(c);
                sums.add(seed.ys);
            }
        }

        List<List<Tok>> anchors = new ArrayList<>();
        List<Double> anchorY = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            List<Tok> c = clusters.get(i);
            int um = 0;
            int money = 0;
            for (Tok t : c) {
                if (isUm(t.text)) {
                    um++;
                } else {
                    money++;
                }
            }
            if (um > 0 || (stopFound && money >= 2)) {
                anchors.add(c);
                anchorY.add(sums.get(i) / c.size());
            }
        }
        if (anchors.isEmpty()) {
            return result;
        }

        double pitch = 3 * medH;
        if (anchorY.size() > 1) {
            List<Double> gaps = new ArrayList<>();
            for (int i = 1; i < anchorY.size(); i++) {
                gaps.add(anchorY.get(i) - anchorY.get(i - 1));
            }
            Collections.sort(gaps);
            pitch = gaps.get(gaps.size() / 2);
        }
        double maxDist = Math.max(1.6 * medH, 0.55 * pitch);

        List<List<Tok>> assigned = new ArrayList<>();
        for (int i = 0; i < anchors.size(); i++) {
            assigned.add(new ArrayList<Tok>(anchors.get(i)));
        }
        Set<Tok> seedSet = new HashSet<>(seeds);
        for (Tok t : zone) {
            if (seedSet.contains(t)) {
                continue;
            }
            int best = -1;
            double bestDist = Double.POSITIVE_INFINITY;
            for (int i = 0; i < anchors.size(); i++) {
                double d = Math.abs(t.ys - anchorY.get(i));
                if (d < bestDist) {
                    bestDist = d;
                    best = i;
                }
            }
            if (best >= 0 && bestDist <= maxDist) {
                assigned.get(best).add(t);
            }
        }

        for (int i = 0; i < anchors.size(); i++) {
            ParsedRow row = buildRow(assigned.get(i), anchorY.get(i), medH, header);
            if (row != null) {
                result.add(row);
            }
        }
        return result;
    }

    /** Testo ricostruito riga per riga (con la correzione di inclinazione), usato per numero bolla e controllo manuale. */
    static String toText(List<OcrWord> input) {
        Analysis analysis = analyze(input);
        if (analysis == null) {
            return "";
        }
        List<Tok> sorted = new ArrayList<>(analysis.tokens);
        sortByY(sorted);
        double tol = Math.max(0.6 * analysis.medH, 5);

        StringBuilder out = new StringBuilder();
        List<Tok> line = new ArrayList<>();
        double sum = 0;
        for (Tok t : sorted) {
            if (!line.isEmpty() && Math.abs(t.ys - sum / line.size()) > tol) {
                appendLine(out, line, analysis.medH);
                line.clear();
                sum = 0;
            }
            line.add(t);
            sum += t.ys;
        }
        if (!line.isEmpty()) {
            appendLine(out, line, analysis.medH);
        }
        return out.toString();
    }

    private static void appendLine(StringBuilder out, List<Tok> line, double medH) {
        Collections.sort(line, new Comparator<Tok>() {
            @Override
            public int compare(Tok a, Tok b) {
                return Double.compare(a.cx, b.cx);
            }
        });
        double prevRight = Double.NaN;
        for (Tok t : line) {
            if (!Double.isNaN(prevRight)) {
                out.append(t.word.getX() - prevRight > 2 * medH ? "  " : " ");
            }
            out.append(t.text);
            prevRight = t.word.getRight();
        }
        out.append('\n');
    }

    private static Analysis analyze(List<OcrWord> input) {
        if (input == null) {
            return null;
        }
        List<Tok> tokens = new ArrayList<>();
        for (OcrWord w : input) {
            String text = cleanToken(w.getText());
            if (text.isEmpty() || w.getWidth() <= 0 || w.getHeight() <= 0) {
                continue;
            }
            if (text.length() == 1 && "lI!/\\".indexOf(text.charAt(0)) >= 0) {
                continue;
            }
            tokens.add(new Tok(w, text));
        }
        if (tokens.size() < 5) {
            return null;
        }

        Analysis analysis = new Analysis();
        analysis.tokens = tokens;
        analysis.medH = medianHeight(tokens);
        analysis.header = findHeader(tokens, analysis.medH);
        double slope = analysis.header == null ? 0 : analysis.header.slope;
        for (Tok t : tokens) {
            t.ys = t.cy - slope * t.cx;
        }
        if (analysis.header != null) {
            double sum = 0;
            for (Tok t : analysis.header.words) {
                sum += t.ys;
            }
            analysis.header.y = sum / analysis.header.words.size();
        }
        return analysis;
    }

    private static Header findHeader(List<Tok> tokens, double medH) {
        Map<Tok, String> keyed = new HashMap<>();
        List<Tok> seeds = new ArrayList<>();
        for (Tok t : tokens) {
            String key = headerKey(t.text);
            if (key != null) {
                keyed.put(t, key);
                if (!"CIVA".equals(key) && !"UM".equals(key) && !"CODICE".equals(key)) {
                    seeds.add(t);
                }
            }
        }
        sortByY(seeds);

        double window = 4 * medH;
        List<Tok> bestGroup = null;
        for (Tok seed : seeds) {
            Map<String, Tok> byKey = new HashMap<>();
            for (Map.Entry<Tok, String> e : keyed.entrySet()) {
                Tok t = e.getKey();
                double dy = Math.abs(t.cy - seed.cy);
                if (dy > window) {
                    continue;
                }
                Tok current = byKey.get(e.getValue());
                if (current == null || dy < Math.abs(current.cy - seed.cy)) {
                    byKey.put(e.getValue(), t);
                }
            }
            int main = 0;
            for (String k : new String[] { "DESC", "QTA", "PREZZO", "SCONTI", "IMPORTO" }) {
                if (byKey.containsKey(k)) {
                    main++;
                }
            }
            if (main >= 2 && byKey.size() >= 3 && (bestGroup == null || byKey.size() > bestGroup.size())) {
                bestGroup = new ArrayList<>(byKey.values());
            }
        }
        if (bestGroup == null) {
            return null;
        }

        Header header = new Header();
        header.words.addAll(bestGroup);
        double refCy = 0;
        for (Tok t : bestGroup) {
            refCy += t.cy;
        }
        refCy /= bestGroup.size();
        for (Tok t : tokens) {
            String letters = letters(t.text);
            if (("ARTICOLO".equals(letters) || "RIGA".equals(letters)) && Math.abs(t.cy - refCy) <= window
                    && !header.words.contains(t)) {
                header.words.add(t);
            }
        }
        for (Tok t : header.words) {
            String key = keyed.get(t);
            String letters = letters(t.text);
            if ("CODICE".equals(key) || "ARTICOLO".equals(letters)) {
                header.codeRight = Math.max(header.codeRight, t.word.getRight());
            }
        }

        header.slope = regressionSlope(header.words);
        return header;
    }

    private static double regressionSlope(List<Tok> words) {
        int n = words.size();
        if (n < 3) {
            return 0;
        }
        double mx = 0;
        double my = 0;
        for (Tok t : words) {
            mx += t.cx;
            my += t.cy;
        }
        mx /= n;
        my /= n;
        double sxx = 0;
        double sxy = 0;
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (Tok t : words) {
            sxx += (t.cx - mx) * (t.cx - mx);
            sxy += (t.cx - mx) * (t.cy - my);
            minX = Math.min(minX, t.cx);
            maxX = Math.max(maxX, t.cx);
        }
        if (sxx == 0 || maxX - minX < 200) {
            return 0;
        }
        double slope = sxy / sxx;
        return Math.abs(slope) > 0.1 ? 0 : slope;
    }

    private static String headerKey(String text) {
        String l = letters(text);
        if (l.startsWith("DESCRIZ")) {
            return "DESC";
        }
        if (l.startsWith("QUANTIT")) {
            return "QTA";
        }
        if (l.startsWith("PREZZ")) {
            return "PREZZO";
        }
        if (l.startsWith("SCONT")) {
            return "SCONTI";
        }
        if (l.startsWith("IMPORT")) {
            return "IMPORTO";
        }
        if (l.equals("CIVA")) {
            return "CIVA";
        }
        if (l.equals("UM")) {
            return "UM";
        }
        if (l.startsWith("CODICE")) {
            return "CODICE";
        }
        return null;
    }

    private static boolean isStopWord(String text) {
        String l = letters(text);
        if (l.length() < 5) {
            return false;
        }
        for (String prefix : STOP_PREFIXES) {
            if (l.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static ParsedRow buildRow(List<Tok> tokens, double anchorY, double medH, Header header) {
        List<Tok> sorted = new ArrayList<>(tokens);
        Collections.sort(sorted, new Comparator<Tok>() {
            @Override
            public int compare(Tok a, Tok b) {
                return Double.compare(a.cx, b.cx);
            }
        });
        double lineTol = Math.max(0.9 * medH, 8);

        Tok um = null;
        for (Tok t : sorted) {
            if (isUm(t.text) && Math.abs(t.ys - anchorY) <= lineTol) {
                boolean hasNumberAfter = false;
                for (Tok n : sorted) {
                    if (n.cx > t.word.getRight() && Math.abs(n.ys - anchorY) <= lineTol && NUMERIC_PATTERN.matcher(n.text).matches()) {
                        hasNumberAfter = true;
                        break;
                    }
                }
                if (hasNumberAfter || um == null) {
                    um = t;
                }
            }
        }

        double numericStart;
        if (um != null) {
            numericStart = um.word.getRight();
        } else {
            numericStart = Double.POSITIVE_INFINITY;
            for (Tok t : sorted) {
                if (isMoney(t.text)) {
                    numericStart = Math.min(numericStart, t.word.getX() - 1);
                }
            }
        }

        List<Tok> numeric = new ArrayList<>();
        List<Tok> text = new ArrayList<>();
        for (Tok t : sorted) {
            if (t == um) {
                continue;
            }
            boolean onLine = Math.abs(t.ys - anchorY) <= lineTol;
            if (onLine && t.cx > numericStart && NUMERIC_PATTERN.matcher(t.text).matches()) {
                numeric.add(t);
            } else if (!(onLine && t.cx > numericStart)) {
                text.add(t);
            }
        }

        ParsedRow row = new ParsedRow();
        row.um = um == null ? "" : normalizeUm(um.text);

        List<String> values = new ArrayList<>();
        for (Tok t : numeric) {
            values.add(t.text);
        }
        if (values.size() >= 2 && IVA_PATTERN.matcher(values.get(values.size() - 1)).matches()
                && !MONEY_PATTERN.matcher(values.get(values.size() - 1)).matches()) {
            row.iva = values.remove(values.size() - 1).replaceAll("[.,]0+$", "");
        } else if (values.size() >= 4 && values.get(values.size() - 1).matches("^\\d{1,2}[.,]0+$")) {
            row.iva = values.remove(values.size() - 1).replaceAll("[.,]0+$", "");
        }

        int n = values.size();
        if (n == 0) {
            return null;
        }
        row.quantita = parseNumber(values.get(0));
        if (n == 2) {
            row.prezzo = parseNumber(values.get(1));
            if (row.quantita != null && row.prezzo != null) {
                row.importo = round2(row.quantita * row.prezzo);
            }
        } else if (n >= 3) {
            row.prezzo = parseNumber(values.get(1));
            row.importo = parseNumber(values.get(n - 1));
            if (n >= 4) {
                StringBuilder sconti = new StringBuilder();
                for (int i = 2; i < n - 1; i++) {
                    if (sconti.length() > 0) {
                        sconti.append('+');
                    }
                    sconti.append(values.get(i));
                }
                row.sconti = sconti.toString();
            }
        }

        List<Tok> codeToks = new ArrayList<>();
        if (header != null && header.codeRight > 0) {
            for (Tok t : text) {
                if (t.cx < header.codeRight + 0.3 * medH) {
                    codeToks.add(t);
                }
            }
        } else if (text.size() >= 2) {
            Tok first = text.get(0);
            for (Tok t : text) {
                if (t.cx < first.cx) {
                    first = t;
                }
            }
            codeToks.add(first);
        }
        text.removeAll(codeToks);
        sortByY(codeToks);
        StringBuilder code = new StringBuilder();
        for (Tok t : codeToks) {
            code.append(t.text);
        }
        row.codice = code.toString();
        row.descrizione = joinByLines(text, medH);

        if (row.descrizione.isEmpty()) {
            row.descrizione = row.codice;
        }
        if (row.quantita == null || row.quantita <= 0 || row.descrizione.isEmpty()) {
            return null;
        }
        return row;
    }

    private static String joinByLines(List<Tok> tokens, double medH) {
        List<Tok> sorted = new ArrayList<>(tokens);
        sortByY(sorted);
        double tol = Math.max(0.6 * medH, 5);
        StringBuilder out = new StringBuilder();
        List<Tok> line = new ArrayList<>();
        double start = 0;
        for (Tok t : sorted) {
            if (!line.isEmpty() && Math.abs(t.ys - start) > tol) {
                flush(out, line);
                line.clear();
            }
            if (line.isEmpty()) {
                start = t.ys;
            }
            line.add(t);
        }
        flush(out, line);
        return out.toString().trim();
    }

    private static void flush(StringBuilder out, List<Tok> line) {
        Collections.sort(line, new Comparator<Tok>() {
            @Override
            public int compare(Tok a, Tok b) {
                return Double.compare(a.cx, b.cx);
            }
        });
        for (Tok t : line) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(t.text);
        }
    }

    private static void sortByY(List<Tok> tokens) {
        Collections.sort(tokens, new Comparator<Tok>() {
            @Override
            public int compare(Tok a, Tok b) {
                return Double.compare(a.ys, b.ys);
            }
        });
    }

    private static double medianHeight(List<Tok> tokens) {
        List<Double> heights = new ArrayList<>();
        for (Tok t : tokens) {
            if (t.text.length() >= 3) {
                heights.add((double) t.word.getHeight());
            }
        }
        if (heights.isEmpty()) {
            for (Tok t : tokens) {
                heights.add((double) t.word.getHeight());
            }
        }
        Collections.sort(heights);
        return Math.max(heights.get(heights.size() / 2), 6);
    }

    private static String cleanToken(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        int start = 0;
        int end = s.length();
        String edge = "|[]{}_~`'\"";
        while (start < end && edge.indexOf(s.charAt(start)) >= 0) {
            start++;
        }
        while (end > start && edge.indexOf(s.charAt(end - 1)) >= 0) {
            end--;
        }
        s = s.substring(start, end).trim();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i))) {
                return s;
            }
        }
        return "";
    }

    private static String letters(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    private static String normalizeUm(String text) {
        String u = text.toUpperCase(Locale.ROOT).replaceAll("[.,]+$", "");
        return "P2".equals(u) ? "PZ" : u;
    }

    private static boolean isUm(String text) {
        return UM_PATTERN.matcher(text.toUpperCase(Locale.ROOT).replaceAll("[.,]+$", "")).matches();
    }

    private static boolean isMoney(String text) {
        return MONEY_PATTERN.matcher(text).matches();
    }

    private static Double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    static Double parseNumber(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replaceAll("[^0-9.,]", "");
        if (s.isEmpty()) {
            return null;
        }
        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                s = s.replace(".", "").replace(',', '.');
            } else {
                s = s.replace(",", "");
            }
        } else if (lastComma >= 0) {
            s = s.replace(',', '.');
        } else if (lastDot >= 0 && s.matches("\\d{1,3}(\\.\\d{3})+")) {
            s = s.replace(".", "");
        }
        int firstDot = s.indexOf('.');
        while (firstDot >= 0 && s.indexOf('.', firstDot + 1) >= 0) {
            s = s.substring(0, firstDot) + s.substring(firstDot + 1);
            firstDot = s.indexOf('.');
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
