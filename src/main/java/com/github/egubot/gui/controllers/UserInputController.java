package com.github.egubot.gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.egubot.io.TextFieldInputStream;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UserInputController {
	private PipedOutputStream pipedOutputStream;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextField inputField;

	@FXML
	private Label promptLabel;

	@FXML
	private Button submitButton;

	@FXML
	public void submit(ActionEvent event) {
		flushText();
	}

	@FXML
	void initialize() {
		assert inputField != null : "fx:id=\"inputField\" was not injected: check your FXML file 'UserInput.fxml'.";
		assert promptLabel != null : "fx:id=\"promptLabel\" was not injected: check your FXML file 'UserInput.fxml'.";
		assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'UserInput.fxml'.";
	}

	public void initialiseInputStream(Stage stage) throws IOException {
		PipedInputStream pipedInputStream = new TextFieldInputStream(getInputField(), stage);
		pipedOutputStream = new PipedOutputStream(pipedInputStream);

		// Set an action listener on the text field to write to the output stream
		getInputField().setOnAction(event -> {
			flushText();
		});

		stage.setOnCloseRequest(event -> {
			event.consume();
			flashWindow(stage);
		});

		System.setIn(pipedInputStream);
	}

	// Method to flash the window to draw user's attention
	private void flashWindow(Stage stage) {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new KeyValue(stage.opacityProperty(), 1)),
				new KeyFrame(Duration.seconds(0.5), new KeyValue(stage.opacityProperty(), 0.5)),
				new KeyFrame(Duration.seconds(1), new KeyValue(stage.opacityProperty(), 1)));
		timeline.setCycleCount(1);
		timeline.play();
	}

	public void flushText() {
		try {
			pipedOutputStream.write(getInputField().getText().getBytes());
			pipedOutputStream.write("\n".getBytes());
			pipedOutputStream.flush();

			getInputField().clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ResourceBundle getResources() {
		return resources;
	}

	public void setResources(ResourceBundle resources) {
		this.resources = resources;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}

	public TextField getInputField() {
		return inputField;
	}

	public void setInputField(TextField inputField) {
		this.inputField = inputField;
	}

	public Label getPromptLabel() {
		return promptLabel;
	}

	public void setPromptLabel(Label promptLabel) {
		this.promptLabel = promptLabel;
	}

	public Button getSubmitButton() {
		return submitButton;
	}

	public void setSubmitButton(Button submitButton) {
		this.submitButton = submitButton;
	}

}
