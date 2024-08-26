package com.github.ollama.api;

import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.storage.ConfigManager;
import java.io.IOException;
import java.util.List;

public class OllamaAI extends AIModel {
	private static final String OLLAMA_URL = "http://localhost:11434/v1/chat/completions";
	static final String OLLAMA_API_KEY = KeyManager.getToken("Ollama_API_Key");

	public OllamaAI() {
		super(getConfigModel(), OLLAMA_API_KEY, OLLAMA_URL, "1", 4096);
	}

	public static String getConfigModel() {
		String model = ConfigManager.getProperty("Ollama_Model");
		if (model == null || model.isBlank()) {
			model = "gemma2:2b";
			ConfigManager.setProperty("Ollama_Model", model);
		}
		return model;
	}

	@Override
	protected String buildRequestBody(String prompt, String author, List<String> conversation) {
		StringBuilder body = new StringBuilder();
		body.append("{\"messages\": [");
		body.append(reformatInput(systemPrompt, "system"));

		if (conversation != null)
			for (String element : conversation) {
				body.append(", " + element);
			}

		body.append(", " + reformatInput(prompt, author) + "]");
		body.append(", \"model\": \"" + model + "\"");
		body.append(", \"stream\": false}");

		return body.toString();
	}

	@Override
	public void setModel(String model) {
		ConfigManager.setProperty("Ollama_Model", model);
		this.model = model;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(
				new OllamaAI().sendRequest("write two paragraphs on any topic", "placeholder", null).getResponse());
	}
}
