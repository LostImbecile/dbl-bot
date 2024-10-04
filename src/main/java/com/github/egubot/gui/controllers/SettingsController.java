package com.github.egubot.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.github.egubot.main.Bot;
import com.github.egubot.storage.ConfigManager;
import com.github.egubot.storage.DataManagerHandler;
import com.github.lavaplayer.SoundPlayback;

import javafx.event.ActionEvent;
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
	private ComboBox<String> storageCBox;

	@FXML
	void chromeProfileDirectoryChange(ActionEvent event) {
		ConfigManager.setProperty("User_Data_Directory", chromeUserDataText.getText().strip());
	}

	@FXML
	void chromeProfileNameChange(ActionEvent event) {
		ConfigManager.setProperty("User_Profile_Name", chromeProfileNameText.getText().strip());
	}

	@FXML
	void cmdChange(ActionEvent event) {
		ConfigManager.setBooleanProperty("CommandLine_Version", cmdToggleButton.selectedProperty().get());
	}

	@FXML
	void dblFeaturesChange(ActionEvent event) {
		ConfigManager.setBooleanProperty("DBL_OFF", !dblegendsToggleButton.selectedProperty().get());
	}

	@FXML
	void playerBufferChange(ActionEvent event) {
		int bufferSize = Integer.parseInt(bufferSizeText.getText());

		ConfigManager.setIntProperty("Player_Buffer_Size_MS", bufferSize);
		SoundPlayback.updateBufferDuration(bufferSize);
	}

	@FXML
	void prefixChange(ActionEvent event) {
		Bot.setPrefix(prefixText.getText());
	}

	@FXML
	void storageTypeChange(ActionEvent event) {
		String selected = storageCBox.getSelectionModel().getSelectedItem();

		switch (selected) {
		case "Local":
			DataManagerHandler.setSQLite(false);
			DataManagerHandler.switchAllManagers();
			break;
		case "SQLite":
			DataManagerHandler.setSQLite(true);
			DataManagerHandler.switchAllManagers();
			break;
		default:
		}
	}

	@FXML
	void initialize() {
		storageCBox.getItems().addAll("Local", "SQLite");

		if (DataManagerHandler.isSQLite())
			storageCBox.getSelectionModel().select(1);
		else
			storageCBox.getSelectionModel().select(0);

		initialisePrefixField();
		initialiseBufferField();
		initialiseChromeLocField();
		initialiseChromeProfileField();
	}

	private void initialiseChromeProfileField() {
		String temp = ConfigManager.getProperty("User_Profile_Name");
		if (temp != null)
			chromeProfileNameText.setText(temp);

		chromeProfileNameText.focusedProperty().addListener((o, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(oldValue)) {
				chromeProfileNameChange(null);
			}
		});
	}

	private void initialiseChromeLocField() {
		String temp = ConfigManager.getProperty("User_Data_Directory");
		if (temp != null)
			chromeUserDataText.setText(temp);

		chromeUserDataText.focusedProperty().addListener((o, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(oldValue)) {
				chromeProfileDirectoryChange(null);
			}
		});
	}

	private void initialiseBufferField() {
		int bufferSize = ConfigManager.getIntProperty("Player_Buffer_Size_MS");
		if (bufferSize < 0) {
			bufferSize = 400;
			ConfigManager.setIntProperty("Player_Buffer_Size_MS", bufferSize);
		}
		bufferSizeText.setText(bufferSize + "");

		bufferSizeText.focusedProperty().addListener((o, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(oldValue)) {
				playerBufferChange(null);
			}
		});
	}

	private void initialisePrefixField() {
		prefixText.focusedProperty().addListener((o, oldValue, newValue) -> {
			if (Boolean.TRUE.equals(oldValue)) {
				prefixChange(null);
			}
		});
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

	public ComboBox<String> getStorageCBox() {
		return storageCBox;
	}

	public void setStorageCBox(ComboBox<String> storageCBox) {
		this.storageCBox = storageCBox;
	}

}
