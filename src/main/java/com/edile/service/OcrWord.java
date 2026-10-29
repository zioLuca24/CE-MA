package com.edile.service;

/** Parola riconosciuta dall'OCR con il suo rettangolo (in pixel dell'immagine elaborata). */
public final class OcrWord {

    private final String text;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public OcrWord(String text, int x, int y, int width, int height) {
        this.text = text == null ? "" : text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRight() {
        return x + width;
    }

    public double centerX() {
        return x + width / 2.0;
    }

    public double centerY() {
        return y + height / 2.0;
    }
}
