package com.github.egubot.gui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.gui.helpers.ListManager;
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
	private Abbreviations savedEmojis;
	private Abbreviations allEmojis;
	private ServerTextChannel channel;
	private String messageType = "normal";

	private List<File> attachedFiles;
	private ListManager listManager;
	private List<String> allChannels;
	private List<String> savedChannels;

	@FXML
	private TextField emojiSearchField;

	@FXML
	private TextField channelSearchField;

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
	private ListView<String> emojiListAll;

	@FXML
	private ListView<String> channelListAll;

	@FXML
	private TextField attachmentTextField;

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
				if (attachedFiles != null && !attachedFiles.isEmpty()) {
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
				String replaced = savedEmojis.replaceReactionIds(message);
				replaced = allEmojis.replaceReactionIds(replaced);
				msg.addReaction(replaced);
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

		initialiseLists();

		textArea.setOnDragOver(this::handleDragOver);
		textArea.setOnDragDropped(this::handleDragDropped);

	}

	private void initialiseLists() {
		List<String> savedEmojiNames;
		List<String> allEmojiNames;
		listManager = new ListManager(Bot.getApi());
		allEmojiNames = listManager.getAllEmojis();
		savedEmojiNames = listManager.getSavedEmojis();
		allChannels = listManager.getAllChannels();
		savedChannels = listManager.getSavedChannels();
		channelMap = listManager.getChannelMap();
		savedEmojis = listManager.getSavedEmojisAbbreviations();
		allEmojis = listManager.getAllEmojisAbbreviations();

		emojiList.getItems().addAll(savedEmojiNames);
		emojiListAll.getItems().addAll(allEmojiNames);
		channelListAll.getItems().addAll(allChannels);
		channelList.getItems().addAll(savedChannels);

		setupEmojiSearch();
		setupChannelSearch();
		setupEmojiSelection();
		setupAllEmojiSelection();
		setupChannelSelection();
		setupSavedChannelSelection();
	}

	private void setupEmojiSearch() {
		emojiSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
			emojiListAll.getItems().clear();
			emojiListAll.getItems().addAll(
					listManager.filterList(allEmojis.getAbbreviationMap().keySet().stream().toList(), newValue));

			emojiList.getItems().clear();
			emojiList.getItems().addAll(
					listManager.filterList(savedEmojis.getAbbreviationMap().keySet().stream().toList(), newValue));
		});
	}

	private void setupChannelSearch() {
		channelSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
			channelListAll.getItems().clear();
			channelListAll.getItems().addAll(listManager.filterList(allChannels, newValue));

			channelList.getItems().clear();
			channelList.getItems().addAll(listManager.filterList(savedChannels, newValue));
		});
	}

	private void setupEmojiSelection() {
		emojiList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				textArea.appendText(savedEmojis.get(newValue));
				Platform.runLater(() -> {
					emojiList.getSelectionModel().clearSelection();
					textArea.requestFocus();
				});
			}
		});
	}

	private void setupAllEmojiSelection() {
	    emojiListAll.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
	        if (newValue != null) {
	            String emojiValue = allEmojis.get(newValue);
	            textArea.appendText(emojiValue);
	            
	            // Save the emoji if it's not already in the saved list
	            if (!savedEmojis.getAbbreviationMap().containsKey(newValue)) {
	                savedEmojis.put(newValue, emojiValue);
	                emojiList.getItems().add(newValue);
	                EmojiManager.addEmoji(newValue, emojiValue);
	            }

	            Platform.runLater(() -> {
	                emojiListAll.getSelectionModel().clearSelection();
	                textArea.requestFocus();
	            });
	        }
	    });
	}

	private void setupChannelSelection() {
		channelListAll.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				updateChannelFromName(newValue);
				Platform.runLater(() -> {
					textArea.requestFocus();
				});
			}
		});
	}

	private void setupSavedChannelSelection() {
		channelList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				updateChannelFromName(newValue);
				Platform.runLater(() -> textArea.requestFocus());
			}
		});
	}

	private void updateChannelFromName(String channelName) {
	    ServerTextChannel selectedChannel = channelMap.get(channelName);
	    if (selectedChannel != null) {
	        channel = selectedChannel;
	        channelNameLabel.setText(channelName);
	        if (!savedChannels.contains(channelName)) {
	            savedChannels.add(channelName);
	            channelList.getItems().add(channelName);
	            SendMessageChannelManager.addChannel(channel.getId());
	        }
	    } else {
	        // Remove the channel from lists if it's no longer accessible
	        allChannels.remove(channelName);
	        savedChannels.remove(channelName);
	        channelList.getItems().remove(channelName);
	    }
	}

	public void updateChannel(String channelID) {
		Bot.getApi().getTextChannelById(channelID).ifPresent(textChannel -> {
			channel = (ServerTextChannel) textChannel;
			String name = listManager.getChannelName(channel);
			updateChannelFromName(name);
		});

		channelIDTextField.clear();
	}

	public void initialiseDefaultChannel() {
		if (!savedChannels.isEmpty()) {
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
