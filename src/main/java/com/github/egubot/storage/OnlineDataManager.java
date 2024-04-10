package com.github.egubot.storage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.interfaces.DataManager;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.main.Bot;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.FileUtilities;

public class OnlineDataManager implements DataManager {
	private static final Logger logger = LogManager.getLogger(OnlineDataManager.class.getName());
	private DiscordApi api;

	private String storageChannelID = KeyManager.getID("Storage_Channel_ID");
	private String storageKey;
	private String storageMsgID;
	private Message storageMessage;

	private BufferedInputStream localInputStream;
	private BufferedInputStream dataInputStream;

	private List<String> data = Collections.synchronizedList(new ArrayList<String>(100));
	private int lockedDataEndIndex = 0;
	private String lastUpdateDate = null;
	private String dataName;

	public OnlineDataManager(String storageKey, InputStream localInput, String dataName) throws IOException {
		this.api = Bot.getApi();
		this.storageKey = storageKey;
		this.storageMsgID = KeyManager.getID(storageKey);
		this.dataName = dataName;
		this.localInputStream = new BufferedInputStream(localInput);
		getStorageMessage();
	}

	public OnlineDataManager(String storageKey, String resourcePath, String dataName) throws IOException {
		this.api = Bot.getApi();
		this.storageKey = storageKey;
		this.storageMsgID = KeyManager.getID(storageKey);
		this.dataName = dataName;
		findLocalInput(resourcePath);
		getStorageMessage();
	}

	private void getStorageMessage() throws IOException {
		try {
			storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).join();
		} catch (Exception e) {
			storageMessage = null;
			checkStorageMessage(true);
		}
	}

	private void findLocalInput(String resourcePath) {
		InputStream fileInputStream = FileUtilities.getFileInputStream(resourcePath, false);
		if (fileInputStream == null) {
			System.err
					.println("\nWarning: no local " + dataName + " data. Expected " + resourcePath + " to be present.");
			logger.warn("No local data.");
		} else {
			localInputStream = new BufferedInputStream(fileInputStream);
		}
	}

	public void initialise(boolean verbose) throws IOException {
		getOnlineData(verbose);
	}

	private void checkStorageMessage(boolean verbose) throws IOException {
		if (storageChannelID.equals("-1"))
			return;
		
		if (api.getTextChannelById(storageChannelID).isPresent()) {
			uploadLocalData(true);
			KeyManager.updateKeys(storageKey, storageMsgID, KeyManager.idsFileName);
			storageMsgID = KeyManager.getID(storageKey);
			try {
				storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get())
						.join();
				if (verbose)
					StreamRedirector.println("events","\nNew " + dataName + " message was created.");
			} catch (Exception e) {
				if (verbose)
					logger.warn("Failed to create new {} message.", dataName);
				logger.error("Failed to create new {} message.", dataName, e);
			}
		} else {
			StreamRedirector.println("prompt","Storage channel ID is invalid, please enter a new one, or -1 to always skip.");

			storageChannelID = Shared.getSystemInput().nextLine();
			KeyManager.updateKeys("Storage_Channel_ID", storageChannelID, KeyManager.idsFileName);
			storageChannelID = KeyManager.getID("Storage_Channel_ID");

			checkStorageMessage(verbose);
		}

	}

	private synchronized void uploadLocalData(boolean fromFile) throws IOException {
		String newID = "";
		String oldID = "";
		try {
			if (fromFile)
				setInputStream(localInputStream);

			try {
				oldID = storageMessage.getContent();
			} catch (Exception e) {
				oldID = "-1";
			}

			newID = api.getTextChannelById(storageChannelID).get()
					.sendMessage(getInputStream(), dataName.replace(" ", "_") + ".txt").join().getIdAsString();

			getInputStream().close();

			try {
				updateStorageMessageContent(newID);
			} catch (Exception e) {
				newID = oldID;
			}

			try {
				// Deletes old data, remove it if you want everything saved
				if (!oldID.equals(storageMsgID) && !oldID.equals(newID)) {
					api.getMessageById(oldID, api.getTextChannelById(storageChannelID).get()).join().delete();
				}
			} catch (Exception e) {
			}

		} catch (Exception e) {
			logger.error("Uploading data failed.", e);
			Thread.currentThread().interrupt();
			checkStorageMessage(true);
		}
	}

	private void updateStorageMessageContent(String newID) {
		if (storageMessage != null) {
			storageMessage.edit(newID).join();
		} else {
			storageMsgID = newID;
			storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).join();
		}
	}

	private void getOnlineData(boolean verbose) throws IOException {
		try {
			Message newMessage;
			try {
				String newID = storageMessage.getContent();
				newMessage = api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
			} catch (Exception e) {
				newMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).join();
			}

			InputStream attachment = newMessage.getAttachments().get(0).asInputStream();

			setInputStream(FileUtilities.toBufferedInputStream(attachment));

			getLastUpdateDate(newMessage);

			if (verbose) {
				StreamRedirector.println("info",
						"\n" + dataName + " data successfully loaded!\nDate of last update: " + lastUpdateDate);
			}

			readInput();

			// Avoid some cases where it's empty
			setInputStream(FileUtilities.toBufferedInputStream(getData()));

		} catch (IOException | NullPointerException e) {
			if (verbose) {
				StreamRedirector.println("info","\n" + dataName + " data failed to load, internal backup used.");
			}
			logger.error("Reading online data failed.", e);
			setInputStream(localInputStream);

			readInput();
		}
	}

	private void getLastUpdateDate(Message newMessage) {
		lastUpdateDate = ConvertObjects.instantToString(newMessage.getCreationTimestamp());
	}

	private void readInput() throws IOException {
		try (BufferedReader br = FileUtilities.getBufferedReader(getInputStream())) {
			String st;

			data.clear();
			Pattern lockedDataPattern = Pattern.compile("(?i)lockeddataindex=(\\d+)$");

			while ((st = br.readLine()) != null) {
				st = st.strip();

				if (st.isBlank())
					break;
				if (lockedDataPattern.matcher(st).matches()) {
					try {
						this.lockedDataEndIndex = Integer.parseInt(st.replaceAll("\\D", ""));
					} catch (Exception e) {
					}
				}

				data.add(st);
			}
		}
	}

	@Override
	public void readData(Messageable e) {
		try {
			getOnlineData(false);
		} catch (IOException e1) {
			logger.error("Failed to read data", e1);
		}
	}

	public void writeData(Messageable e) {
		try {
			setInputStream(FileUtilities.toBufferedInputStream(getData()));
			uploadLocalData(false);

			if (e != null)
				e.sendMessage("Updated <:drink:1184466286944735272>");
		} catch (Exception e1) {
			if (e != null)
				e.sendMessage("Couldn't update <:sad:1020780174901522442>");

			logger.error("Failed to write and upload data.", e1);
		}
	}

	public void sendData(Messageable e) {
		try {
			e.sendMessage(FileUtilities.toBufferedInputStream(getData()), dataName.replace(" ", "_") + ".txt").join();
		} catch (Exception e1) {
			e.sendMessage("Failed");
			logger.error("Failed to send data online.", e1);
		}
	}

	private BufferedInputStream getInputStream() {
		return dataInputStream;
	}

	private void setInputStream(BufferedInputStream dataInputStream) {
		this.dataInputStream = dataInputStream;
	}

	public int getLockedDataEndIndex() {
		return lockedDataEndIndex;
	}

	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		this.lockedDataEndIndex = lockedDataEndIndex;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = Collections.synchronizedList(data);
	}

	public BufferedInputStream getLocalInputStream() {
		return localInputStream;
	}

	public void setLocalInputStream(BufferedInputStream localInputStream) {
		this.localInputStream = localInputStream;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

}
