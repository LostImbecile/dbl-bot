package com.openai.chatgpt;

import java.io.IOException;
import com.github.egubot.ai.AIModel;
import com.github.egubot.managers.KeyManager;

public class ChatGPT extends AIModel{
	static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
	static final String API_KEY = KeyManager.getToken("ChatGPT_API_Key");

	public ChatGPT(){
		super("gpt-3.5-turbo", API_KEY, GPT_URL, 4096);
	}
	public static void main(String[] args) throws IOException {
		System.out.println(new ChatGPT().sendRequest("", "placeholder", null).getResponse());
	}

}
