package com.edile.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class SimpleImageOcr {

    private static final int MAX_SIDE = 2200;
    private static final int PAGE_SEG_MODE_SINGLE_BLOCK = 6;
    private static final Map<String, ITesseract> ENGINES = new HashMap<>();

    private SimpleImageOcr() {
    }

    public static synchronized String extractText(BufferedImage image) {
        BufferedImage prepared = normalizeImage(image);
        Throwable lastError = null;

        String[] languages = new String[] { "ita", "eng" };
        for (String language : languages) {
            try {
                return engine(language).doOCR(prepared);
            } catch (TesseractException ex) {
                lastError = ex;
            } catch (Throwable ex) {
                lastError = ex;
            }
        }

        throw new IllegalArgumentException(
                "OCR non disponibile su questo server (errore nativo). Carica il PDF originale oppure usa importazione strutturata.",
                lastError
        );
    }

    private static ITesseract engine(String language) {
        ITesseract tesseract = ENGINES.get(language);
        if (tesseract != null) {
            return tesseract;
        }
        tesseract = new Tesseract();
        String tessdataPrefix = System.getenv("TESSDATA_PREFIX");
        if (tessdataPrefix != null && !tessdataPrefix.trim().isEmpty()) {
            tesseract.setDatapath(tessdataPrefix);
        }
        tesseract.setLanguage(language);
        tesseract.setPageSegMode(PAGE_SEG_MODE_SINGLE_BLOCK);
        tesseract.setVariable("preserve_interword_spaces", "1");
        ENGINES.put(language, tesseract);
        return tesseract;
    }

    private static BufferedImage normalizeImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        double scale = 1.0;
        int max = Math.max(width, height);
        if (max > MAX_SIDE) {
            scale = (double) MAX_SIDE / (double) max;
        }

        int targetW = Math.max(1, (int) Math.round(width * scale));
        int targetH = Math.max(1, (int) Math.round(height * scale));

        BufferedImage normalized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = normalized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, targetW, targetH, null);
        g.dispose();
        return normalized;
    }
}