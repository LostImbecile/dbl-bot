package com.github.egubot.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class KeyManager {

	public static String tokensFileName = "Tokens.txt";
	public static String idsFileName = "IDs.txt";

	private KeyManager() {
	}

	public static String getToken(String apiKey) {

		/*
		 * Add a Tokens.txt file in your resources or
		 * next to the bot and put your tokens in it.
		 * Use something to identify them, example:
		 * Discord_API_Key=wefwefweffw
		 * ChatGPT_API_Key=ewefewfewfw
		 */
		InputStream input = getFile(tokensFileName);

		if (input == null) {
			System.err.println("Couldn't find or create tokens file");
			return "";
		}

		return findKey(input, apiKey);
	}

	public static String getID(String idKey) {
		// Add an IDs.txt file in your resources or next to the bot
		InputStream input = getFile(idsFileName);

		if (input == null) {
			System.err.println("Couldn't find or create IDs file");
			return "";
		}

		return findKey(input, idKey);
	}

	private static String findKey(InputStream input, String key) {

		String st = "";
		Scanner in = new Scanner(input);
		while (in.hasNextLine()) {
			st = in.nextLine().replaceAll("[ =]", "");
			if (st.contains(key)) {
				st = st.replace(key, "");
				break;
			}
		}
		in.close();
		return st;
	}

	private static InputStream getFile(String fileName) {
		InputStream input;
		File file = new File(fileName);

		if (file.exists()) {
			input = createNewFile(fileName, file);

		} else {

			input = KeyManager.class.getResourceAsStream("/" + fileName);

			if (input == null) {
				input = createNewFile(fileName, file);
			}
		}

		return input;
	}

	private static InputStream createNewFile(String fileName, File file) {
		try {
			file.createNewFile();
			return new FileInputStream(fileName);
		} catch (Exception e1) {
			return null;
		}
	}

	public static void checkKeys() {
		String token = KeyManager.getToken("Discord_API_Key");
		String storageChannelID = KeyManager.getID("Storage_Channel_ID");
		String testServerID = KeyManager.getID("Test_Server_ID");
		String statusChannelID = KeyManager.getID("Status_Channel_ID");

		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		String id;

		if (token.equals("")) {
			System.out.println("No token, please enter one below:");
			id = in.nextLine();
			updateKeys("Discord_API_Key", id, tokensFileName);
		} else if (token.equals("-1")) {
			System.out.println("Invalid token, please enter one below:");
			id = in.nextLine();
			updateKeys("Discord_API_Key", id, tokensFileName);
		}
		if (!(testServerID.length() >= 17 && testServerID.matches("[\\d+]+")) && !testServerID.equals("-1")) {
			System.out.println("No valid storage server ID, please enter one below, or -1 to always skip.");
			id = in.nextLine();
			updateKeys("Test_Server_ID", id, idsFileName);
		}

		if (!(storageChannelID.length() >= 17 && storageChannelID.matches("[\\d+]+"))
				&& !storageChannelID.equals("-1")) {
			System.out.println("No valid storage channel ID, please enter one below, or -1 to always skip.");
			id = in.nextLine();
			updateKeys("Storage_Channel_ID", id, idsFileName);
		}

		if (!(statusChannelID.length() >= 17 && statusChannelID.matches("[\\d+]+")) && !statusChannelID.equals("-1")) {
			System.out.println("No valid status channel ID, please enter one below, or -1 to always skip.");
			id = in.nextLine();
			updateKeys("Status_Channel_ID", id, idsFileName);
		}

	}

	public static void updateKeys(String key, String id, String fileName) {
		InputStream input = getFile(fileName);
		ArrayList<String> data = new ArrayList<>(0);

		data.add(key + "=" + id);

		String st = "";
		Scanner in = new Scanner(input);
		while (in.hasNextLine()) {
			st = in.nextLine();
			if (!(st.equals("") || st.equals("\n") || st.contains(key)))
				data.add(st);
		}
		in.close();

		try (FileWriter output = new FileWriter(fileName)) {
			for (int i = 0; i < data.size(); i++) {
				output.write(data.get(i).replace("\n", "") + "\n");
			}
		} catch (IOException e) {
			System.err.println("Couldn't update keys.");
		}

	}
}
