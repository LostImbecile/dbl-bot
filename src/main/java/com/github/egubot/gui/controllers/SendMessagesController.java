package com.github.egubot.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class SendMessagesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField channelIDTextField;

    @FXML
    private ListView<String> channelList;

    @FXML
    private Label channelNameLabel;

    @FXML
    private ListView<String> emojiList;

    @FXML
    private TextField reactIDTextField;

    @FXML
    private ComboBox<String> replyReactSelect;

    @FXML
    private TextArea textArea;
    
    @FXML
    void channelIDFieldOnKeyPressed(KeyEvent event) {

    }

    @FXML
    void messageIDFieldOnKeyPressed(KeyEvent event) {

    }

    @FXML
    void textAreaOnKeyPressed(KeyEvent event) {

    }

    @FXML
    void initialize() {
        assert channelIDTextField != null : "fx:id=\"channelIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert channelList != null : "fx:id=\"channelList\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert channelNameLabel != null : "fx:id=\"channelNameLabel\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert emojiList != null : "fx:id=\"emojiList\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert reactIDTextField != null : "fx:id=\"reactIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert replyReactSelect != null : "fx:id=\"replyReactSelect\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'SendMessages.fxml'.";

        replyReactSelect.getItems().addAll("normal","reply", "react", "edit", "delete");
        replyReactSelect.getSelectionModel().select(0);
        
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

	public TextField getChannelIDTextField() {
		return channelIDTextField;
	}

	public void setChannelIDTextField(TextField channelIDTextField) {
		this.channelIDTextField = channelIDTextField;
	}

	public ListView<String> getChannelList() {
		return channelList;
	}

	public void setChannelList(ListView<String> channelList) {
		this.channelList = channelList;
	}

	public Label getChannelNameLabel() {
		return channelNameLabel;
	}

	public void setChannelNameLabel(Label channelNameLabel) {
		this.channelNameLabel = channelNameLabel;
	}

	public ListView<String> getEmojiList() {
		return  emojiList;
	}

	public void setEmojiList(ListView<String> emojiList) {
		this.emojiList = emojiList;
	}

	public TextField getReactIDTextField() {
		return reactIDTextField;
	}

	public void setReactIDTextField(TextField reactIDTextField) {
		this.reactIDTextField = reactIDTextField;
	}

	public ComboBox<String> getReplyReactSelect() {
		return  replyReactSelect;
	}

	public void setReplyReactSelect(ComboBox<String> replyReactSelect) {
		this.replyReactSelect = replyReactSelect;
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(TextArea textArea) {
		this.textArea = textArea;
	}

}

