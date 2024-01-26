package com.github.egubot.shared;

import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONUtilities {
	static Random rng = new Random();
	static Gson gson = new Gson();

	public static String prettify(String jsonText) {
		JsonObject jsonObject = JsonParser.parseString(jsonText).getAsJsonObject();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonObject);
	}

	public static String jsonify(String input) {
		String st = gson.toJson(input);
		return st.substring(1, st.length() - 1);
		// input = input.replace("\\", "\\\\");
		// input = input.replace("\n", "\\n").replace("\"", "\\\"").replace("\t",
		// "\\t").replace("/", "\\/");
	}

	public static String dejsonify(String input) {
		return gson.fromJson("\"" + input + "\"", String.class);
	}

	public static String generateId() {
		long timestamp = System.currentTimeMillis();
		int random = rng.nextInt(1000); // Adjust the range as needed
		return String.format("%d-%d", timestamp, random);
	}

	public static void main(String[] args) {
		String st = jsonify("wwewe\"\n\ts");
		System.out.println(st);
		st = dejsonify(st);
		System.out.println(st);
	}

}
