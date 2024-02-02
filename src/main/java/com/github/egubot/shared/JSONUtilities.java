package com.github.egubot.shared;

import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONUtilities {
	static Random rng = new Random();

	public static String prettify(String jsonText) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonObject jsonObject = JsonParser.parseString(jsonText).getAsJsonObject();
		
		return gson.toJson(jsonObject);
	}

	public static String jsonify(String input) {
		Gson gson = new Gson();
		String st = gson.toJson(input);
		return st.substring(1, st.length() - 1);
		// input = input.replace("\\", "\\\\");
		// input = input.replace("\n", "\\n").replace("\"", "\\\"").replace("\t",
		// "\\t").replace("/", "\\/");
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

}
