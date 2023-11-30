package com.github.egubot.build;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	private BufferedInputStream localInputStream;
	private BufferedInputStream dataInputStream;

	// This file is created to send the data to discord
	private File tempDataFile = new File("TempData.txt");

	private List<String> data = new ArrayList<>(0);
	private int lockedDataEndIndex = 0;
	private String lastUpdateDate = null;
	private String dataName;

	public OnlineDataManager(DiscordApi api, String storageKey, String dataName, InputStream localInput,
			boolean verbose) throws Exception {
		this.api = api;
		this.storageKey = storageKey;
		this.storageMsgID = KeyManager.getID(storageKey);
		this.dataName = dataName;
		this.localInputStream = new BufferedInputStream(localInput);
		initialise(verbose);
	}

	public OnlineDataManager(DiscordApi api, String storageKey, String resourcePath, String dataName, boolean verbose)
			throws Exception {
		this.api = api;
		this.storageKey = storageKey;
		this.storageMsgID = KeyManager.getID(storageKey);
		this.dataName = dataName;
		findLocalInput(resourcePath);
		initialise(verbose);
	}

	private void findLocalInput(String resourcePath) {
		try {
			localInputStream = new BufferedInputStream(getClass().getResourceAsStream(resourcePath));
			if (localInputStream == null) {
				localInputStream = new BufferedInputStream(new FileInputStream(new File(resourcePath)));
			}
		} catch (Exception e) {
			System.err
					.println("\nWarning: no local " + dataName + " data. Expected " + resourcePath + " to be present.");
			localInputStream = null;
		}
	}

	private void initialise(boolean verbose) throws Exception {
		try {
			storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).get();

		} catch (Exception e) {
			storageMessage = null;
			checkStorageMessage(verbose);
		}

		getOnlineData(verbose);
	}

	private void checkStorageMessage(boolean verbose) throws Exception {
		if (!storageChannelID.equals("-1")) {
			uploadLocalData(true);
			KeyManager.updateKeys(storageKey, storageMsgID, KeyManager.idsFileName);
			storageMsgID = KeyManager.getID(storageKey);
			try {
				storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).get();
				if (verbose)
					System.out.println("\nNew " + dataName + " message was created.");
			} catch (Exception e) {
				if (verbose)
					System.err.println("\nFailed to create new " + dataName + " message.");
			}
		}
	}

	private void uploadLocalData(boolean fromFile) throws Exception {
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

			// System.out.println(getInputStream().available()/1024 + "KB");

			newID = api.getTextChannelById(storageChannelID).get()
					.sendMessage(getInputStream(), dataName.replace(" ", "_") + ".txt").join().getIdAsString();

			try {
				if (storageMessage != null) {
					storageMessage.edit(newID).join();
				}
				else {
					storageMsgID = newID;
					storageMessage = api.getMessageById(storageMsgID, api.getTextChannelById(storageChannelID).get()).get();
				}
			} catch (Exception e) {
				newID = oldID;
			}

			getInputStream().close();
			getOnlineData(false);

			try {
				// Deletes old data, remove it if you want everything saved
				if (!oldID.equals(storageMsgID) && !oldID.equals(newID)) {
					api.getMessageById(oldID, api.getTextChannelById(storageChannelID).get()).join().delete();
				}
			} catch (Exception e) {
			}

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

	private void getOnlineData(boolean verbose) throws IOException {
		try {
			String newID = storageMessage.getContent();

			try {
				api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
			} catch (Exception e) {
				newID = storageMsgID;
			}

			String[] date;

			Message newMessage = api.getMessageById(newID, api.getTextChannelById(storageChannelID).get()).join();
			InputStream attachment = newMessage.getAttachments().get(0).asInputStream();

			setInputStream(new BufferedInputStream(attachment));

			// Date
			date = newMessage.getCreationTimestamp().toString().split("[Tz.]");
			lastUpdateDate = date[0] + ", " + date[1].substring(0, date[1].length() - 3);

			if (verbose) {
				System.out.println(
						"\n" + dataName + " data successfully loaded!\nDate of last update: " + lastUpdateDate);
			}

			readInput();

			// Avoid some cases where it's empty
			setInputStream(new BufferedInputStream(attachment));

		} catch (IOException | NullPointerException e) {
			if (verbose) {
				System.err.println("\n" + dataName + " data failed to load, internal backup used.");
			}

			setInputStream(localInputStream);

			readInput();
		}
	}

	private void readInput() throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream()))) {
			String st;

			data.clear();
			while ((st = br.readLine()) != null) {
				st = st.trim().replace("\n", "").replace("é", "e");

				if (st.toLowerCase().matches("lockeddataindex=\\d+$")) {
					try {
						this.lockedDataEndIndex = Integer.parseInt(st.replace("lockeddataindex=", ""));
					} catch (Exception e) {
					}
				} else if (!st.equals(""))
					data.add(st);
			}
		}
	}

	public void writeData(Messageable e) throws Exception {
		try {
			try (FileWriter file = new FileWriter(tempDataFile)) {
				for (int i = 0; i < data.size(); i++) {
					file.write(data.get(i).replace("\n", "") + "\n");
				}

				file.write("LockedDataIndex=" + this.lockedDataEndIndex);
			}

			setInputStream(new BufferedInputStream(new FileInputStream(tempDataFile)));

			uploadLocalData(false);

			if (e != null) {
				if (!tempDataFile.delete())
					e.sendMessage("Couldn't update <:sad:1020780174901522442>");
				else
					e.sendMessage("Updated <:legudrink:804071006956159008>");
			} else {
				if (!tempDataFile.delete())
					System.out.println("Couldn't update");
			}
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
			textChannel.sendMessage(new BufferedInputStream(newMessage.getAttachments().get(0).asInputStream()),
					dataName.replace(" ", "_") + ".txt").join();
		} catch (Exception e) {
			System.err.println("No");
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

	void setData(List<String> data) {
		this.data = data;
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
