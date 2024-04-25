package com.github.egubot.facades;

import com.openai.chatgpt.ChatGPT;

public class ChatGPTContext extends AIContext {
	
	static {
		setModel(new ChatGPT());
	}
}
