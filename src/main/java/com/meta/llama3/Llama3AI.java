package com.meta.llama3;

import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;
import java.io.IOException;

public class Llama3AI extends AIModel{
	private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
	static final String GROQ_API_KEY = KeyManager.getToken("Groq_API_Key");

	public Llama3AI() {
		super("llama3-8b-8192", GROQ_API_KEY, GROQ_URL, "1.2", 8196);
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new Llama3AI().sendRequest("say something", "placeholder", null).getResponse());
	}

}