package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatgptResponseCommand implements Command {

	@Override
	public String getName() {
		return "gpt";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(!AIContext.getChatGPT().isAIOn())
			return false;
		
		AIContext.getChatGPT().respond(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Send a message to ChatGPT and get an AI-generated response";
	}

	@Override
	public String getUsage() {
		return getName() + " <message>";
	}

	@Override
	public String getCategory() {
		return "AI";
	}
}