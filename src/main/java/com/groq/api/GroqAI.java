package com.groq.api;

import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.storage.ConfigManager;

import java.io.IOException;

public class GroqAI extends AIModel{
	private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
	static final String GROQ_API_KEY = KeyManager.getToken("Groq_API_Key");

	public GroqAI() {
		super(getConfigModel(), GROQ_API_KEY, GROQ_URL, "1.2", 8196);
	}
	
	public static String getConfigModel() {
		String model = ConfigManager.getProperty("Groq_Model");
		if (model == null || model.isBlank()) {
			model = "llama3-8b-8192";
			ConfigManager.setProperty("Groq_Model", model);
		}
		return model;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new GroqAI().sendRequest("say something", "placeholder", null).getResponse());
	}

}