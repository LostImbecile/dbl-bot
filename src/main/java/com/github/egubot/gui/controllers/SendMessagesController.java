package com.github.egubot.gui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.EmojiManager;
import com.github.egubot.managers.SendMessageChannelManager;
import com.github.egubot.objects.Abbreviations;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

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
	private TextField attachmentTextField;

	private List<File> attachedFiles;

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

		Bot.getApi().getTextChannelById(channelID).ifPresent(textChannel -> {
			channel = Bot.getApi().getServerTextChannelById(channelID).get();
			String name = getChannelName(channel);
			if (channelMap.get(name) == null) {
				channelMap.put(name, channel);
				channelList.getItems().add(0, name);
				channelList.getSelectionModel().selectFirst();

				SendMessageChannelManager.addChannel(channel.getId());
			} else {
				channelList.getSelectionModel().select(name);
			}
		});

		channelIDTextField.clear();
	}

	public static String getChannelName(ServerTextChannel channel) {
		return ServerInfoUtilities.getServer(channel).getName() + " -> " + channel.getName();
	}

	@FXML
	void changeMessageID(ActionEvent event) {
		Platform.runLater(() -> textArea.requestFocus());
	}

	@FXML
	void textAreaOnKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			event.consume();
			if (event.isShiftDown()) {
				int caretPosition = textArea.getCaretPosition();
				textArea.insertText(caretPosition, "\n");
			} else {
				int caretPosition = textArea.getCaretPosition();
				textArea.deleteText(caretPosition - 1, caretPosition);
				submitMessage();
			}
		} else if (event.isControlDown() && event.getCode() == KeyCode.V) {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			if (clipboard.hasFiles()) {
				if (attachedFiles == null)
					attachedFiles = new ArrayList<>();
				attachedFiles.addAll(clipboard.getFiles());
				updateAttachmentTextField();
				event.consume();
			}
		}
	}

	private void updateAttachmentTextField() {
		StringBuilder fileNames = new StringBuilder();

		if (attachedFiles != null && !attachedFiles.isEmpty()) {
			for (File file : attachedFiles) {
				if (fileNames.length() > 0)
					fileNames.append(", ");
				fileNames.append(file.getName());
			}
		}

		attachmentTextField.setText(fileNames.toString());
	}

	private void handleDragOver(DragEvent event) {
		if (event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		}
		event.consume();
	}

	private void handleDragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			if (attachedFiles == null)
				attachedFiles = new ArrayList<>();
			attachedFiles.addAll(db.getFiles());
			updateAttachmentTextField();
			success = true;
		}
		event.setDropCompleted(success);
		event.consume();
	}

	private void submitMessage() {
		String message = textArea.getText();
		textArea.clear();

		try {
			Message msg = getMessage();
			switch (messageType) {
			case "normal":
				if (attachedFiles != null && !attachedFiles.isEmpty() ) {
					channel.sendMessage(message, attachedFiles.toArray(new File[0]));
					clearAttachments();
				} else {
					channel.sendMessage(message);
				}
				break;
			case "delete":
				msg.delete();
				break;
			case "reply":
				msg.reply(message);
				break;
			case "react":
				msg.addReaction(emojis.replaceReactionIds(Abbreviations.getReactionId(message)));
				break;
			case "edit":
				msg.edit(message);
				break;
			default:
				// Do nothing
			}
			messageTypeCombo.getSelectionModel().select("normal");
			clearAttachments();
		} catch (Exception e) {
			textArea.setText("Failed.");
			e.printStackTrace();
		}
	}

	private void clearAttachments() {
		attachedFiles = null;
		attachmentTextField.clear();
	}

	public Message getMessage() throws InterruptedException, ExecutionException {
		if (messageIDTextField.getText().isBlank()) {
			return null;
		}
		return Bot.getApi().getMessageById(messageIDTextField.getText(), channel).get();
	}

	@FXML
	void initialize() {
		messageTypeCombo.getItems().addAll("normal", "reply", "react", "edit", "delete");
		messageTypeCombo.getSelectionModel().select(0);

		channelList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				channel = channelMap.get(newValue);
				channelNameLabel.setText(newValue);
			}
			Platform.runLater(() -> textArea.requestFocus());
		});

		emojis = EmojiManager.getAllEmojis();

		emojiList.getItems().addAll(emojis.getAbbreviationMap().keySet());

		emojiList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				textArea.appendText(emojis.get(newValue));
				Platform.runLater(() -> emojiList.getSelectionModel().clearSelection());
				Platform.runLater(() -> textArea.requestFocus());
			}
		});

		textArea.setOnDragOver(this::handleDragOver);
		textArea.setOnDragDropped(this::handleDragDropped);
	}

	public void initialiseDefaultChannel() {
		Set<Long> channels = SendMessageChannelManager.getAllChannels();

		if (!channels.isEmpty()) {
			// Load channels for the servers
			for (Long channelID : channels) {
				Bot.getApi().getServerTextChannelById(channelID).ifPresentOrElse(temp -> {
					String channelName = getChannelName(temp);
					channelMap.put(channelName, temp);
					channelList.getItems().add(channelName);
				}, () -> SendMessageChannelManager.removeChannel(channelID));
			}

		}

		if (!channelList.getItems().isEmpty()) {
			channelList.getSelectionModel().selectFirst();
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
