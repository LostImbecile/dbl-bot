package com.github.egubot.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class SendMessagesController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField channelIDTextField;

    @FXML
    private ListView<?> channelList;

    @FXML
    private Label channelNameLabel;

    @FXML
    private ListView<?> emojiList;

    @FXML
    private TextField reactIDTextField;

    @FXML
    private ComboBox<?> replyReactSelect;

    @FXML
    private TextArea textArea;

    @FXML
    void initialize() {
        assert channelIDTextField != null : "fx:id=\"channelIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert channelList != null : "fx:id=\"channelList\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert channelNameLabel != null : "fx:id=\"channelNameLabel\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert emojiList != null : "fx:id=\"emojiList\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert reactIDTextField != null : "fx:id=\"reactIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert replyReactSelect != null : "fx:id=\"replyReactSelect\" was not injected: check your FXML file 'SendMessages.fxml'.";
        assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'SendMessages.fxml'.";

    }

}

