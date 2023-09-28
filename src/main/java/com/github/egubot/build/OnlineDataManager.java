package com.github.egubot.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import com.github.egubot.main.KeyManager;

public class OnlineDataManager {

	private DiscordApi api;

	private String storageChannelID = KeyManager.getID("Storage_Channel_ID");
	private String storageKey;
	private String storageMsgID;
	private Message storageMessage;

	private InputStream localInputStream;
	private InputStream dataInputStream;

	// This file is created to send the data to discord
	private File tempDataFile = new File("TempData.txt");

	private List<String> data = new ArrayList<>(0);
	private int lockedDataEndIndex;
	private String dataName;

	public OnlineDataManager(DiscordApi api, String storageKey, String resourcePath, String dataName) throws Exception {
		this.api = api;
		this.storageKey = storageKey;
		this.storageMsgID = KeyManager.getID(storageKey);
		this.dataName = dataName;
		findLocalInput(resourcePath);
		initialise();
	}

	private void findLocalInput(String resourcePath) {
		try {
			localInputStream = getClass().getResourceAsStream(resourcePath);
			if (localInputStream == null) {
				localInputStream = new FileInputStream(new File(resourcePath));
			}
		} catch (Exception e) {
			System.err.println("\nWarning: no local " + dataName + " data. Expected " + resourcePath +" to be present.");
			localInputStream = null;
		}
	}

	private void initialise() throws Exception {
		try {
			storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).get();

		} catch (Exception e) {
			checkStorageMessage();
		}

		getOnlineData(true);
	}

	private void checkStorageMessage() throws Exception {
		if (!storageChannelID.equals("-1")) {
			uploadLocalData(true);
			KeyManager.updateKeys(storageKey, storageMsgID, KeyManager.idsFileName);
			storageMsgID = KeyManager.getID(storageKey);
			try {
				storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).get();
				System.out.println("\nNew " + dataName + " message was created.");
			} catch (Exception e) {
				System.err.println("\nFailed to create new " + dataName + " message.");
			}
		}
	}

	private void uploadLocalData(boolean fromFile) throws Exception {
		String newID = "";
		try {
			if (fromFile)
				setInputStream(localInputStream);

			try {
				newID = storageMessage.getContent();

				// Deletes old data, remove it if you want everything saved
				if (!newID.equals(storageMsgID)) {
					api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join().delete();
				}
			} catch (Exception e) {

			}

			newID = api.getTextChannelById(storageChannelID).get()
					.sendMessage(getInputStream(), dataName.replace(" ", "_") + ".txt").join().getIdAsString();
			
			try {
				storageMessage.edit(newID).join();
			} catch (Exception e) {
				System.err.println("\nOnline data ID failed to update");
			}

			getInputStream().close();
			getOnlineData(false);
		} catch (Exception e) {
			checkStorageChannel();
		}
	}

	private void checkStorageChannel() throws Exception {
		if (!storageChannelID.equals("-1")) {
			if (api.getTextChannelById(storageChannelID).isPresent()) {
				throw new IOException();
			} else {
				System.err.println("\nStorage channel ID is invalid, please enter a new one, or -1 to always skip.");
				@SuppressWarnings("resource")
				Scanner in = new Scanner(System.in);

				storageChannelID = in.nextLine();
				KeyManager.updateKeys("Storage_Channel_ID", storageChannelID, KeyManager.idsFileName);
				storageChannelID = KeyManager.getID("Storage_Channel_ID");
			}
		}
	}

	private void getOnlineData(boolean verbose) {
		try {
			String newID = storageMessage.getContent();

			try {
				api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
			} catch (Exception e) {
				newID = storageMsgID;
			}

			String[] date;

			Message newMessage = api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
			setInputStream(newMessage.getAttachments().get(0).asInputStream());

			if (verbose) {
				date = newMessage.getCreationTimestamp().toString().split("[Tz.]");
				System.out.println("\n" + dataName + " data successfully loaded!\nDate of last update: " + date[0]
						+ ", " + date[1].substring(0, date[1].length() - 3));
			}

			Scanner read = new Scanner(getInputStream());
			String st;

			data.clear();
			while (read.hasNextLine()) {
				st = read.nextLine().trim().replace("\n", "");
				if (!st.equals(""))
					data.add(st);
			}
			read.close();

		} catch (IOException | NullPointerException e) {
			System.err.println("\n" + dataName + " data failed to load, internal backup used.");

			setInputStream(localInputStream);
		}
	}

	public void writeData(Messageable e) throws Exception {
		try {
			try (FileWriter file = new FileWriter(tempDataFile)) {
				for (int i = 0; i < data.size(); i++)
					file.write(data.get(i).replace("\n", "") + "\n");

			}
			setInputStream(new FileInputStream(tempDataFile));

			uploadLocalData(false);
			if (!tempDataFile.delete())
				e.sendMessage("Couldn't update <:sad:1020780174901522442>");
			else
				e.sendMessage("Updated <:legudrink:804071006956159008>");
		} catch (Exception e1) {
			System.err.println("Failed to write and upload data.");
		}
	}

	public void sendData(Messageable textChannel) {
		String newID = storageMessage.getContent();
		if (!newID.matches("[\\d+]+"))
			newID = storageMsgID;

		Message newMessage = api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
		try {
			textChannel.sendMessage(newMessage.getAttachments().get(0).asInputStream(),
					dataName.replace(" ", "_") + ".txt").join();
		} catch (Exception e) {
			System.err.println("No");
		}
	}

	private InputStream getInputStream() {
		return dataInputStream;
	}

	private void setInputStream(InputStream dataInputStream) {
		this.dataInputStream = dataInputStream;
	}

	public int getLockedDataEndIndex() {
		return lockedDataEndIndex;
	}

	public void setLockedDataEndIndex(int lockedDataEndIndex) {
		this.lockedDataEndIndex = lockedDataEndIndex;
	}

	List<String> getData() {
		return data;
	}

	void setData(List<String> data) {
		this.data = data;
	}

	public InputStream getLocalInputStream() {
		return localInputStream;
	}

	public void setLocalInputStream(InputStream localInputStream) {
		this.localInputStream = localInputStream;
	}
}
