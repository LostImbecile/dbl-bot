package com.github.egubot.gui.controllers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.features.SendMessagesFromConsole;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.Abbreviations;
import com.github.egubot.shared.Shared;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SendMessagesController {
	private Map<String, ServerTextChannel> channelMap = new HashMap<>();
	private Abbreviations emojis;
	private ServerTextChannel channel;
	private String messageType = "normal";

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
	private TextField messageIDTextField;

	@FXML
	private ComboBox<String> messageTypeCombo;

	@FXML
	private TextArea textArea;

	@FXML
	void messageSendTypeChange(ActionEvent event) {
		messageType = messageTypeCombo.getSelectionModel().getSelectedItem();
		Platform.runLater(() -> textArea.requestFocus());
	}

	@FXML
	void changeChannelID(ActionEvent event) {
		String temp = channelIDTextField.getText();
		updateChannel(temp);

		Platform.runLater(() -> textArea.requestFocus());

	}

	public void updateChannel(String channelID) {
		if (channelID.length() >= 17) {
			Bot.getApi().getTextChannelById(channelID).ifPresentOrElse(textChannel -> {
				channel = Bot.getApi().getServerTextChannelById(channelID).get();
				String name = getChannelName();
				channelNameLabel.setText(name);
				channelMap.put(name, channel);
				channelList.getItems().add(0, name);
				channelList.getSelectionModel().selectFirst();

			}, () -> channelIDTextField.clear());

		} else {
			channelIDTextField.clear();
		}
	}

	public String getChannelName() {
		return ServerInfoUtilities.getServer(channel).getName() + " -> " + channel.getName();
	}

	@FXML
	void changeMessageID(ActionEvent event) {
		Platform.runLater(() -> textArea.requestFocus());
	}

	@FXML
	void textAreaOnKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			if (event.isShiftDown()) {
				textArea.appendText("\n");
			} else {
				event.consume();
				submitMessage();
			}
		}
	}

	private void submitMessage() {
		String message = textArea.getText();
		textArea.clear();

		try {
			if (messageType.equals("normal")) {
				channel.sendMessage(message);
				return;
			}

			Message msg = getMessage();

			switch (messageType) {
			case "delete":
				msg.delete();
				break;
			case "reply":
				msg.reply(message);
				break;
			case "react":
				msg.addReaction(emojis.replaceReactionIds(message));
				break;
			case "edit":
				msg.edit(message);
				break;
			default:

			}
		} catch (Exception e) {
			textArea.setText("Failed.");
		}

	}

	public Message getMessage() throws InterruptedException, ExecutionException {
		return Bot.getApi().getMessageById(messageIDTextField.getText(), channel).get();
	}

	@FXML
	void initialize() {
		assert channelIDTextField != null
				: "fx:id=\"channelIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert channelList != null
				: "fx:id=\"channelList\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert channelNameLabel != null
				: "fx:id=\"channelNameLabel\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert emojiList != null : "fx:id=\"emojiList\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert messageIDTextField != null
				: "fx:id=\"reactIDTextField\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert messageTypeCombo != null
				: "fx:id=\"replyReactSelect\" was not injected: check your FXML file 'SendMessages.fxml'.";
		assert textArea != null : "fx:id=\"textArea\" was not injected: check your FXML file 'SendMessages.fxml'.";

		messageTypeCombo.getItems().addAll("normal", "reply", "react", "edit", "delete");
		messageTypeCombo.getSelectionModel().select(0);

		channelList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				channel = channelMap.get(newValue);
			}
			Platform.runLater(() -> textArea.requestFocus());
		});

		emojis = SendMessagesFromConsole.getEmojis();

		emojiList.getItems().addAll(emojis.getAbbreviationMap().keySet());

		emojiList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				textArea.appendText(emojis.getAbbreviationId(newValue));
				Platform.runLater(() -> emojiList.getSelectionModel().clearSelection());
				Platform.runLater(() -> textArea.requestFocus());
			}
		});

	}

	public void initialiseDefaultChannel() {
		String defaultChannelID = KeyManager.getID("Default_Message_Channel_ID");
		if (!Bot.getApi().getTextChannelById(defaultChannelID).isPresent()) {
			StreamRedirector.println("prompt", "No default starting channel was set, enter a channel ID below:");
			KeyManager.updateKeys("Default_Message_Channel_ID", Shared.getSystemInput().nextLine(),
					KeyManager.idsFileName);
			defaultChannelID = KeyManager.getID("Default_Message_Channel_ID");
		}

		try {
			updateChannel(defaultChannelID);
		} catch (Exception e) {
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
		return emojiList;
	}

	public void setEmojiList(ListView<String> emojiList) {
		this.emojiList = emojiList;
	}

	public TextField getReactIDTextField() {
		return messageIDTextField;
	}

	public void setReactIDTextField(TextField reactIDTextField) {
		this.messageIDTextField = reactIDTextField;
	}

	public ComboBox<String> getReplyReactSelect() {
		return messageTypeCombo;
	}

	public void setReplyReactSelect(ComboBox<String> replyReactSelect) {
		this.messageTypeCombo = replyReactSelect;
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(TextArea textArea) {
		this.textArea = textArea;
	}

}
