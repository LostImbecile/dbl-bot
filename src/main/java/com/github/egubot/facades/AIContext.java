package com.github.egubot.facades;

import com.github.egubot.ai.AIModelHandler;
import com.meta.llama3.Llama3AI;
import com.openai.chatgpt.ChatGPT;

public class AIContext{
	private static final AIModelHandler llama3 =  new AIModelHandler(new Llama3AI());
	private static final AIModelHandler gpt3 =  new AIModelHandler(new ChatGPT());
	
	private AIContext() {	
	}
	
	public static AIModelHandler getLlama3() {
		return llama3;
	}

	public static AIModelHandler getGpt3() {
		return gpt3;
	}

}
