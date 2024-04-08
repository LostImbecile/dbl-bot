package com.github.egubot.io;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class LabelOutputStream extends OutputStream {
	 private final Label label;

	    public LabelOutputStream(Label label) {
	        this.label = label;
	    }
	    
	    @Override
	    public void write(byte[] b, int off, int len) {
	        String text = new String(b, off, len, StandardCharsets.UTF_8);
	        Platform.runLater(() ->label.setText(text));
	    }

	    @Override
	    public void write(int b) {
	        // Single byte write, convert it to String
	        write(new byte[]{(byte) b}, 0, 1);
	    }
}
