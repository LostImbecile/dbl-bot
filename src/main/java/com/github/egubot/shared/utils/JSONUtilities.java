package com.github.egubot.shared.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JSONUtilities {
	static Random rng = new Random();

	public static String prettify(String jsonText) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonElement je = JsonParser.parseString(jsonText);
		if (je.isJsonObject()) {
			return gson.toJson(je.getAsJsonObject());
		} else if (je.isJsonArray()) {
			return gson.toJson(je.getAsJsonArray());
		}
		return null;
	}

	public static String jsonify(String input) {
		Gson gson = new Gson();
		String st = gson.toJson(input);
		return st.substring(1, st.length() - 1);
	}

	public static String dejsonify(String input) {
		Gson gson = new Gson();
		return gson.fromJson("\"" + input + "\"", String.class);
	}

	public static String generateId() {
		long timestamp = System.currentTimeMillis();
		int random = rng.nextInt(1000); // Adjust the range as needed
		return String.format("%d-%d", timestamp, random);
	}

	public static <T> String toJsonPrettyPrint(T object, Class<T> clazz) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		return gson.toJson(object, clazz);
	}

	public static <T> T jsonToClass(String jsonTxt, Class<T> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jsonTxt, clazz);
	}

	public static void writeListToJson(List<?> list, String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(list, writer);
		} catch (IOException e) {
			FileUtilities.logger.error("Failed to write list to file.", e);
		}
	}

	public static <T> List<T> readListFromJson(String fileName, Class<T[]> type) {
		try (InputStream input = FileUtilities.getFileInputStream(fileName, true)) {
			Gson gson = new Gson();
			T[] array = gson.fromJson(new InputStreamReader(input), type);
			if (array != null) {
				return new ArrayList<>(List.of(array));
			}
		} catch (IOException e) {
			FileUtilities.logger.error("Failed to read list from file.", e);
		}
		return new ArrayList<>();
	}

}
