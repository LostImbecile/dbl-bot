package com.github.egubot.facades;

import com.github.egubot.build.DefaultSystemPromptManager;

public class DefaultSystemPromptContext {
	private static DefaultSystemPromptManager manager = new DefaultSystemPromptManager();
	
	private DefaultSystemPromptContext() {
	}
	
	public static String getDefaultSystemPrompt() {
		return manager.getDefaultSystemPrompt();
	}
	
	public static void setDefaultSystemPrompt(String prompt) {
		manager.setDefaultSystemPrompt(prompt);
	}
	
	public static void reloadDefaultPrompt() {
		manager.reloadDefaultPrompt();
	}
}