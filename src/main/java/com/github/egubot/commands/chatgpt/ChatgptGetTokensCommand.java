package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatgptGetTokensCommand implements Command {

	@Override
	public String getName() {
		return "gpt tokens";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage("```java\nTokens Used: \"" + AIContext.getChatGPT().getLastTokens(msg) + "\"```");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Get current token usage statistics for ChatGPT interactions";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "AI";
	}
}