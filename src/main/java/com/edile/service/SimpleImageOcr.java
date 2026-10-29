package com.edile.service;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SimpleImageOcr {

    public static final int PSM_AUTO = 3;
    public static final int PSM_SINGLE_BLOCK = 6;
    public static final int PSM_SPARSE = 11;

    private static final int MAX_SIDE = 2400;
    private static final int BACKGROUND_BLOCK = 8;
    private static final int BACKGROUND_RADIUS = 5;
    private static final float MIN_WORD_CONFIDENCE = 10f;
    private static final Map<String, ITesseract> ENGINES = new HashMap<>();

    private SimpleImageOcr() {
    }

    public static synchronized String extractText(BufferedImage image) {
        BufferedImage prepared = normalizeImage(image);
        Throwable lastError = null;

        String[] languages = new String[] { "ita", "eng" };
        for (String language : languages) {
            try {
                return engine(language, PSM_SINGLE_BLOCK).doOCR(prepared);
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

    /** Parole con coordinate: servono a ricostruire righe e colonne della tabella anche su foto inclinate. */
    public static synchronized List<OcrWord> extractWords(BufferedImage image, int pageSegMode) {
        BufferedImage prepared = normalizeImage(image);
        Throwable lastError = null;

        String[] languages = new String[] { "ita", "eng" };
        for (String language : languages) {
            try {
                List<Word> words = engine(language, pageSegMode).getWords(prepared, ITessAPI.TessPageIteratorLevel.RIL_WORD);
                List<OcrWord> result = new ArrayList<>();
                for (Word word : words) {
                    Rectangle box = word.getBoundingBox();
                    if (box == null || word.getConfidence() < MIN_WORD_CONFIDENCE) {
                        continue;
                    }
                    result.add(new OcrWord(word.getText(), box.x, box.y, box.width, box.height));
                }
                return result;
            } catch (Throwable ex) {
                lastError = ex;
            }
        }

        throw new IllegalArgumentException(
                "OCR non disponibile su questo server (errore nativo). Carica il PDF originale oppure usa importazione strutturata.",
                lastError
        );
    }

    private static ITesseract engine(String language, int pageSegMode) {
        String key = language + ":" + pageSegMode;
        ITesseract tesseract = ENGINES.get(key);
        if (tesseract != null) {
            return tesseract;
        }
        tesseract = new Tesseract();
        String tessdataPrefix = System.getenv("TESSDATA_PREFIX");
        if (tessdataPrefix != null && !tessdataPrefix.trim().isEmpty()) {
            tesseract.setDatapath(tessdataPrefix);
        }
        tesseract.setLanguage(language);
        tesseract.setPageSegMode(pageSegMode);
        tesseract.setVariable("preserve_interword_spaces", "1");
        ENGINES.put(key, tesseract);
        return tesseract;
    }

    static BufferedImage normalizeImage(BufferedImage source) {
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
        flattenBackground(normalized);
        return normalized;
    }

    /** Uniforma ombre e illuminazione irregolare delle foto: ogni pixel viene rapportato al bianco locale della carta. */
    private static void flattenBackground(BufferedImage gray) {
        int w = gray.getWidth();
        int h = gray.getHeight();
        byte[] pixels = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
        int sw = (w + BACKGROUND_BLOCK - 1) / BACKGROUND_BLOCK;
        int sh = (h + BACKGROUND_BLOCK - 1) / BACKGROUND_BLOCK;
        if (sw < 3 || sh < 3) {
            return;
        }

        float[] blockMax = new float[sw * sh];
        for (int by = 0; by < sh; by++) {
            for (int bx = 0; bx < sw; bx++) {
                int x0 = bx * BACKGROUND_BLOCK;
                int y0 = by * BACKGROUND_BLOCK;
                int x1 = Math.min(w, x0 + BACKGROUND_BLOCK);
                int y1 = Math.min(h, y0 + BACKGROUND_BLOCK);
                int max = 0;
                for (int y = y0; y < y1; y++) {
                    for (int x = x0; x < x1; x++) {
                        max = Math.max(max, pixels[y * w + x] & 0xFF);
                    }
                }
                blockMax[by * sw + bx] = max;
            }
        }

        float[] background = new float[sw * sh];
        for (int by = 0; by < sh; by++) {
            for (int bx = 0; bx < sw; bx++) {
                float max = 0;
                for (int dy = -BACKGROUND_RADIUS; dy <= BACKGROUND_RADIUS; dy++) {
                    int yy = Math.min(sh - 1, Math.max(0, by + dy));
                    for (int dx = -BACKGROUND_RADIUS; dx <= BACKGROUND_RADIUS; dx++) {
                        int xx = Math.min(sw - 1, Math.max(0, bx + dx));
                        max = Math.max(max, blockMax[yy * sw + xx]);
                    }
                }
                background[by * sw + bx] = Math.max(max, 1f);
            }
        }

        float[] smooth = new float[sw * sh];
        for (int pass = 0; pass < 2; pass++) {
            for (int by = 0; by < sh; by++) {
                for (int bx = 0; bx < sw; bx++) {
                    float sum = 0;
                    int count = 0;
                    for (int dy = -2; dy <= 2; dy++) {
                        int yy = by + dy;
                        if (yy < 0 || yy >= sh) {
                            continue;
                        }
                        for (int dx = -2; dx <= 2; dx++) {
                            int xx = bx + dx;
                            if (xx >= 0 && xx < sw) {
                                sum += background[yy * sw + xx];
                                count++;
                            }
                        }
                    }
                    smooth[by * sw + bx] = sum / count;
                }
            }
            float[] swap = background;
            background = smooth;
            smooth = swap;
        }

        for (int y = 0; y < h; y++) {
            int rowBase = (y / BACKGROUND_BLOCK) * sw;
            for (int x = 0; x < w; x++) {
                float bg = background[rowBase + x / BACKGROUND_BLOCK];
                int value = (int) (255f * (pixels[y * w + x] & 0xFF) / bg);
                pixels[y * w + x] = (byte) Math.min(255, value);
            }
        }
    }
}
