package com.openai.chatgpt;

import java.io.IOException;
import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.storage.ConfigManager;

public class ChatGPT extends AIModel{
	static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
	static final String API_KEY = KeyManager.getToken("ChatGPT_API_Key");

	public ChatGPT(){
		super(getConfigModel(), API_KEY, GPT_URL, 4096);
	}
	
	public static String getConfigModel() {
		String model = ConfigManager.getProperty("ChatGPT_Model");
		if (model == null || model.isBlank()) {
			model = "gpt-4o-mini";
			ConfigManager.setProperty("ChatGPT_Model", model);
		}
		return model;
	}
	public static void main(String[] args) throws IOException {
		System.out.println(new ChatGPT().sendRequest("", "placeholder", null).getResponse());
	}

}
