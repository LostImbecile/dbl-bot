package com.openai.chatgpt;

import java.io.IOException;
import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.storage.ConfigManager;

public class ChatGPT extends AIModel {
	static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
	private static final String GPT_MODEL_LIST_URL = "https://api.openai.com/v1/models";
	static final String API_KEY = KeyManager.getToken("ChatGPT_API_Key");

	public ChatGPT() {
		super(getConfigModel(), API_KEY, GPT_URL, 4096);
		this.setListModelsURL(GPT_MODEL_LIST_URL);
	}

	public static String getConfigModel() {
		String model = ConfigManager.getProperty("ChatGPT_Model");
		if (model == null || model.isBlank()) {
			model = "gpt-4o-mini";
			ConfigManager.setProperty("ChatGPT_Model", model);
		}
		return model;
	}

	@Override
	public void setModel(String model) {
		ConfigManager.setProperty("ChatGPT_Model", model);
		this.model = model;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new ChatGPT().sendRequest("", "placeholder", null).getResponse());
	}

}
