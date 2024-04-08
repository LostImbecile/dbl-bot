package com.github.egubot.logging;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PipedInputStream;

public class TextFieldInputStream extends PipedInputStream {
    private Stage stage;
    private TextField textField;

    public TextFieldInputStream(TextField textField, Stage stage) throws IOException {
        super();
        this.textField = textField;
        this.stage = stage;
    }

    @Override
    public synchronized int read() throws IOException {
        // Show the stage when reading
        Platform.runLater(() -> {
            if (!stage.isShowing()) {
                stage.show();
            }
            stage.toFront();
            textField.requestFocus();
        });

        return super.read();
    }
}