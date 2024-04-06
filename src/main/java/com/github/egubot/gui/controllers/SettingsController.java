package com.github.egubot.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField bufferSizeText;

    @FXML
    private TextField chromeProfileNameText;

    @FXML
    private TextField chromeUserDataText;

    @FXML
    private RadioButton cmdToggleButton;

    @FXML
    private RadioButton dblegendsToggleButton;

    @FXML
    private TextField prefixText;

    @FXML
    private ComboBox<?> storageCBox;

    @FXML
    void initialize() {
        assert bufferSizeText != null : "fx:id=\"bufferSizeText\" was not injected: check your FXML file 'Settings.fxml'.";
        assert chromeProfileNameText != null : "fx:id=\"chromeProfileNameText\" was not injected: check your FXML file 'Settings.fxml'.";
        assert chromeUserDataText != null : "fx:id=\"chromeUserDataText\" was not injected: check your FXML file 'Settings.fxml'.";
        assert cmdToggleButton != null : "fx:id=\"cmdToggleButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert dblegendsToggleButton != null : "fx:id=\"dblegendsToggleButton\" was not injected: check your FXML file 'Settings.fxml'.";
        assert prefixText != null : "fx:id=\"prefixText\" was not injected: check your FXML file 'Settings.fxml'.";
        assert storageCBox != null : "fx:id=\"storageCBox\" was not injected: check your FXML file 'Settings.fxml'.";

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

	public TextField getBufferSizeText() {
		return bufferSizeText;
	}

	public void setBufferSizeText(TextField bufferSizeText) {
		this.bufferSizeText = bufferSizeText;
	}

	public TextField getChromeProfileNameText() {
		return chromeProfileNameText;
	}

	public void setChromeProfileNameText(TextField chromeProfileNameText) {
		this.chromeProfileNameText = chromeProfileNameText;
	}

	public TextField getChromeUserDataText() {
		return chromeUserDataText;
	}

	public void setChromeUserDataText(TextField chromeUserDataText) {
		this.chromeUserDataText = chromeUserDataText;
	}

	public RadioButton getCmdToggleButton() {
		return cmdToggleButton;
	}

	public void setCmdToggleButton(RadioButton cmdToggleButton) {
		this.cmdToggleButton = cmdToggleButton;
	}

	public RadioButton getDblegendsToggleButton() {
		return dblegendsToggleButton;
	}

	public void setDblegendsToggleButton(RadioButton dblegendsToggleButton) {
		this.dblegendsToggleButton = dblegendsToggleButton;
	}

	public TextField getPrefixText() {
		return prefixText;
	}

	public void setPrefixText(TextField prefixText) {
		this.prefixText = prefixText;
	}

	public ComboBox<?> getStorageCBox() {
		return storageCBox;
	}

	public void setStorageCBox(ComboBox<?> storageCBox) {
		this.storageCBox = storageCBox;
	}
    
}
