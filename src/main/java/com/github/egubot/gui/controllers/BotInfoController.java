package com.github.egubot.gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.egubot.commands.botcontrol.RefreshCommand;
import com.github.egubot.main.Bot;
import com.github.egubot.main.Restart;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BotInfoController {
	private Stage settingsStage = null;
	private Stage sendMessagesStage = null;
	private SendMessagesController sendMessagesController = null;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private VBox buttonsVbox;

	@FXML
	private Button copyInviteButton;

	@FXML
	private TextArea eventsArea;

	@FXML
	private TextArea debugTextArea;

	@FXML
	private Button helpButton;

	@FXML
	private Button refreshButton;

	@FXML
	private TextArea infoArea;

	@FXML
	private TextArea logsArea;

	@FXML
	private TextArea javacordLogsArea;

	@FXML
	private Button sendMessagesButton;

	@FXML
	private Button settingsButton;

	@FXML
	private Button restartButton;

	@FXML
	void restart(ActionEvent event) {
		Restart.restart();
	}

	@FXML
	void copyBotInvite(ActionEvent event) {
		ClipboardContent content = new ClipboardContent();
		content.putString(Bot.getInvite());

		Clipboard.getSystemClipboard().setContent(content);
	}

	@FXML
	void refreshStorage(ActionEvent event) {
		RefreshCommand.refresh();
	}

	@FXML
	void openHelpWindow(ActionEvent event) {
		// not yet implemented
	}

	@FXML
	void openSendMessagesWindow(ActionEvent event) {
		try {
			initialiseSendMessagesStage();
			sendMessagesStage.show();
			sendMessagesStage.toFront();

			sendMessagesController.initialiseDefaultChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialiseSendMessagesStage() throws IOException {
		FXMLLoader sendMessagesLoader = new FXMLLoader(getClass().getResource("/fxml/SendMessages.fxml"));
		Parent sendMessagesRootRoot = sendMessagesLoader.load();
		sendMessagesController = sendMessagesLoader.getController();

		sendMessagesStage = new Stage();
		sendMessagesStage.setTitle("Send Messages With Bot");
		setIcon(sendMessagesStage);

		sendMessagesStage.setMinHeight(420 + 20);
		sendMessagesStage.setMinWidth(600 + 20);

		Scene scene = new Scene(sendMessagesRootRoot);
		scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
		sendMessagesStage.setScene(scene);
	}

	@FXML
	void openSettingsWindow(ActionEvent event) {
		try {
			initialiseSettingsStage();
			settingsStage.show();
			settingsStage.toFront();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initialiseSettingsStage() throws IOException {
		if (settingsStage == null) {
			FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
			Parent settingsRoot = settingsLoader.load();
			settingsStage = new Stage();
			settingsStage.setTitle("Settings");
			setIcon(settingsStage);

			settingsStage.setMinHeight(420 + 20);
			settingsStage.setMinWidth(600 + 20);

			Scene scene = new Scene(settingsRoot);
			scene.getStylesheets().add(getClass().getResource("/css/root.css").toExternalForm());
			settingsStage.setScene(scene);
		}
	}

	public void setIcon(Stage stage) {
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/discordIcon.png")));
	}

	@FXML
	void initialize() {
		assert buttonsVbox != null : "fx:id=\"buttonsVbox\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert copyInviteButton != null
				: "fx:id=\"copyInviteButton\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert debugTextArea != null : "fx:id=\"debugTextArea\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert eventsArea != null : "fx:id=\"eventsArea\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert helpButton != null : "fx:id=\"helpButton\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert infoArea != null : "fx:id=\"infoArea\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert javacordLogsArea != null
				: "fx:id=\"javacordLogsArea\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert logsArea != null : "fx:id=\"logsArea\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert refreshButton != null : "fx:id=\"refreshButton\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert restartButton != null : "fx:id=\"restartButton\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert sendMessagesButton != null
				: "fx:id=\"sendMessagesButton\" was not injected: check your FXML file 'BotInfo.fxml'.";
		assert settingsButton != null
				: "fx:id=\"settingsButton\" was not injected: check your FXML file 'BotInfo.fxml'.";

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

	public Button getCopyInviteButton() {
		return copyInviteButton;
	}

	public void setCopyInviteButton(Button copyInviteButton) {
		this.copyInviteButton = copyInviteButton;
	}

	public TextArea getEventsArea() {
		return eventsArea;
	}

	public void setEventsArea(TextArea eventsArea) {
		this.eventsArea = eventsArea;
	}

	public Button getHelpButton() {
		return helpButton;
	}

	public void setHelpButton(Button helpButton) {
		this.helpButton = helpButton;
	}

	public TextArea getInfoArea() {
		return infoArea;
	}

	public void setInfoArea(TextArea infoArea) {
		this.infoArea = infoArea;
	}

	public TextArea getLogsArea() {
		return logsArea;
	}

	public void setLogsArea(TextArea logsArea) {
		this.logsArea = logsArea;
	}

	public Button getSendMessagesButton() {
		return sendMessagesButton;
	}

	public void setSendMessagesButton(Button sendMessagesButton) {
		this.sendMessagesButton = sendMessagesButton;
	}

	public Button getSettingsButton() {
		return settingsButton;
	}

	public void setSettingsButton(Button settingsButton) {
		this.settingsButton = settingsButton;
	}

	public VBox getButtonsVbox() {
		return buttonsVbox;
	}

	public void setButtonsVbox(VBox buttonsVbox) {
		this.buttonsVbox = buttonsVbox;
	}

	public TextArea getJavacordLogsArea() {
		return javacordLogsArea;
	}

	public void setJavacordLogsArea(TextArea javacordLogsArea) {
		this.javacordLogsArea = javacordLogsArea;
	}

	public TextArea getDebugTextArea() {
		return debugTextArea;
	}

	public void setDebugTextArea(TextArea debugTextArea) {
		this.debugTextArea = debugTextArea;
	}

}
