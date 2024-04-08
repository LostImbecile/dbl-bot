package com.github.egubot.managers;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.objects.KeyValue;
import com.github.egubot.shared.FileUtilities;
import com.github.egubot.shared.Shared;

public class KeyManager {
	private static final Logger logger = LogManager.getLogger(KeyManager.class.getName());
	public static String tokensFileName = "Tokens.txt";
	public static String idsFileName = "IDs.txt";

	private KeyManager() {
	}

	// Keys shouldn't have "=" in their name, their values can
	public static String getToken(String tokenKey) {

		/*
		 * Add a Tokens.txt file in your resources or
		 * next to the bot and put your tokens in it.
		 * Use something to identify them, example:
		 * Discord_API_Key=wefwefweffw
		 * ChatGPT_API_Key=ewefewfewfw
		 */
		InputStream input = FileUtilities.getFileInputStream(tokensFileName, true);

		if (input == null) {
			StreamRedirector.println("","Couldn't find or create tokens file");
			return "";
		}

		return findKey(input, tokenKey);
	}

	public static String getID(String idKey) {
		// Add an IDs.txt file in your resources or next to the bot
		InputStream input = FileUtilities.getFileInputStream(idsFileName, true);

		if (input == null) {
			StreamRedirector.println("","Couldn't find or create IDs file");
			return "";
		}

		return findKey(input, idKey);
	}

	public static List<KeyValue> readKeys(InputStream input) {
		List<KeyValue> keyValueList = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			String key;
			String value;
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isBlank() && line.contains("=")) {
					String[] parts = line.split("=");

					if (parts.length >= 2) {
						key = parts[0].strip();
						value = line.substring(line.indexOf("=") + 1);
						
						keyValueList.add(new KeyValue(key, value));
					}

				}
			}
		} catch (IOException e) {
			logger.fatal("Error reading input files.", e);
		}

		return keyValueList;
	}

	public static String findKey(InputStream file, String key) {
		if (key.isBlank())
			return "";

		ArrayList<KeyValue> keyValueList = (ArrayList<KeyValue>) readKeys(file);
		for (KeyValue keyValue : keyValueList) {
			if (keyValue.getTokenKey().equals(key)) {
				return keyValue.getValue();
			}
		}
		return "";
	}

	public static void updateKeys(String key, String id, String fileName) {
		InputStream input = FileUtilities.getFileInputStream(fileName, true);
		ArrayList<KeyValue> keyValues = (ArrayList<KeyValue>) readKeys(input);

		// Update or add the new key-value pair
		boolean found = false;
		for (KeyValue keyValue : keyValues) {
			if (keyValue.getTokenKey().equals(key)) {
				keyValue.setValue(id);
				found = true;
				break;
			}
		}
		if (!found) {
			keyValues.add(new KeyValue(key, id));
		}

		// Write the updated key-values back to the file
		try (FileWriter output = new FileWriter(fileName)) {
			for (KeyValue keyValue : keyValues) {
				output.write(keyValue.getTokenKey() + "=" + keyValue.getValue() + "\n");
			}
		} catch (IOException e) {
			logger.error("Error updating input files.", e);
			StreamRedirector.println("","Couldn't update keys.");
		}
	}

	public static void checkKeys() {
		String token = KeyManager.getToken("Discord_API_Key");
		String testServerID = KeyManager.getID("Test_Server_ID");
		String statusChannelID = KeyManager.getID("Status_Channel_ID");

		Scanner in = Shared.getSystemInput();
		String id;

		if (token.isBlank()) {
			StreamRedirector.println("","No token, please enter one below:");
			id = in.nextLine();
			updateKeys("Discord_API_Key", id, tokensFileName);
		} else if (token.equals("-1")) {
			StreamRedirector.println("","Invalid token, please enter one below:");
			id = in.nextLine();
			updateKeys("Discord_API_Key", id, tokensFileName);
		}
		if (!(testServerID.length() >= 17 && testServerID.matches("[\\d+]+")) && !testServerID.equals("-1")) {
			StreamRedirector.println("","No valid Test server ID, please enter one below, or -1 to always skip.");
			id = in.nextLine();
			updateKeys("Test_Server_ID", id, idsFileName);
		}

		if (!(statusChannelID.length() >= 17 && statusChannelID.matches("[\\d+]+")) && !statusChannelID.equals("-1")) {
			StreamRedirector.println("","No valid status channel ID, please enter one below, or -1 to always skip.");
			id = in.nextLine();
			updateKeys("Status_Channel_ID", id, idsFileName);
		}

	}
	
	public static void main(String[] args) {
		readKeys(FileUtilities.getFileInputStream(idsFileName, true));
	}

}
