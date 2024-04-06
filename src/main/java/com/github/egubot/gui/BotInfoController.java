package com.github.egubot.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class BotInfoController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button copyInviteButton;

    @FXML
    private TextArea eventsArea;

    @FXML
    private Button helpButton;

    @FXML
    private TextArea infoArea;

    @FXML
    private TextArea logsArea;

    @FXML
    private Button sendMessagesButton;

    @FXML
    private Button settingsButton;

    @FXML
    void initialize() {
        assert copyInviteButton != null : "fx:id=\"copyInviteButton\" was not injected: check your FXML file 'test.fxml'.";
        assert eventsArea != null : "fx:id=\"eventsArea\" was not injected: check your FXML file 'test.fxml'.";
        assert helpButton != null : "fx:id=\"helpButton\" was not injected: check your FXML file 'test.fxml'.";
        assert infoArea != null : "fx:id=\"infoArea\" was not injected: check your FXML file 'test.fxml'.";
        assert logsArea != null : "fx:id=\"logsArea\" was not injected: check your FXML file 'test.fxml'.";
        assert sendMessagesButton != null : "fx:id=\"sendMessagesButton\" was not injected: check your FXML file 'test.fxml'.";
        assert settingsButton != null : "fx:id=\"settingsButton\" was not injected: check your FXML file 'test.fxml'.";

    }

	public TextArea getLogsArea() {
		return logsArea;
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

	public void setLogsArea(TextArea logsArea) {
		this.logsArea = logsArea;
	}

}
