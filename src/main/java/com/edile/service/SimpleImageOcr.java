package com.edile.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class SimpleImageOcr {

    private static final int MAX_SIDE = 2200;

    private SimpleImageOcr() {
    }

    public static String extractText(BufferedImage image) {
        BufferedImage prepared = normalizeImage(image);
        Throwable lastError = null;

        String[] languages = new String[] { "ita+eng", "eng" };
        for (String language : languages) {
            ITesseract tesseract = new Tesseract();
            String tessdataPrefix = System.getenv("TESSDATA_PREFIX");
            if (tessdataPrefix != null && !tessdataPrefix.trim().isEmpty()) {
                tesseract.setDatapath(tessdataPrefix);
            }
            tesseract.setLanguage(language);
            try {
                return tesseract.doOCR(prepared);
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

        BufferedImage normalized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = normalized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, targetW, targetH, null);
        g.dispose();
        return normalized;
    }
}