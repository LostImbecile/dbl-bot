package com.github.egubot.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONUtilities {

	public static String prettify(String jsonText) {
		JsonObject jsonObject = JsonParser.parseString(jsonText).getAsJsonObject();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(jsonObject);
	}
	
	public static String jsonify(String input) {
		// new Gson().toJson(input) also works
		input = input.replace("\\", "\\\\");
		input = input.replace("\n", "\\n").replace("\"", "\\\"").replace("\t", "\\t").replace("/", "\\/");
		return input.strip();
	}

	public static String dejsonify(String input) {
		input = input.replace("\\\\", "\\");
		input = input.replace("\\n", "\n").replace("\\\"", "\"").replace("\\t", "\t").replace("\\/", "/");
		return input.strip();
	}
	
}
