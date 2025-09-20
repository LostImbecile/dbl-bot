package com.github.egubot.commands.gemini;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GeminiGetTokensCommand implements Command {

	@Override
	public String getName() {
		return "gem tokens";
	}

	@Override
	public String getDescription() {
		return "Get current token usage statistics for Gemini AI interactions";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage("```java\nTokens Used: \"" + AIContext.getGemini().getLastTokens(msg) + "\"```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}