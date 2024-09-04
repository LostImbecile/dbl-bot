package com.github.egubot.facades;

import com.github.egubot.ai.AIModel;
import com.github.egubot.ai.AIModelHandler;
import com.github.ollama.api.OllamaAI;
import com.groq.api.GroqAI;
import com.groq.api.GroqLLavaAI;
import com.openai.chatgpt.ChatGPT;

public class AIContext{
	private static final AIModelHandler groqText =  new AIModelHandler(new GroqAI());
	private static final AIModelHandler chatGPT =  new AIModelHandler(new ChatGPT());
	private static final AIModelHandler ollama =  new AIModelHandler(new OllamaAI());
	private static final AIModel groqVision =  new GroqLLavaAI();
	
	private AIContext() {	
	}
	
	public static AIModelHandler getGroq() {
		return groqText;
	}

	public static AIModelHandler getChatGPT() {
		return chatGPT;
	}
	
	public static AIModelHandler getOllama() {
		return ollama;
	}
	
	public static AIModel getGroqVision() {
		return groqVision;
	}

}
