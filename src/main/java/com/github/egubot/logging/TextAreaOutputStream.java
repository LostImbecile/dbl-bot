package com.github.egubot.logging;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TextAreaOutputStream extends OutputStream {
    private final TextArea textArea;

    public TextAreaOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }
    
    public void reset() {
    	textArea.clear();
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len, StandardCharsets.UTF_8);
        Platform.runLater(() ->  textArea.appendText(text));
    }

    @Override
    public void write(int b) {
        // Single byte write, convert it to String
        write(new byte[]{(byte) b}, 0, 1);
    }
}
