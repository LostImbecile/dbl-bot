package com.github.egubot.logging;

import javafx.scene.control.TextArea;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CustomOutputStream extends OutputStream {
    private final TextArea textArea;

    public CustomOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len, StandardCharsets.UTF_8);
        textArea.appendText(text);
    }

    @Override
    public void write(int b) {
        // Single byte write, convert it to String
        write(new byte[]{(byte) b}, 0, 1);
    }
}
