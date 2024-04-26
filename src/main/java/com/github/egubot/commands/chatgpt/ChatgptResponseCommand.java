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
		if(!AIContext.getGpt3().isAIOn())
			return false;
		
		AIContext.getGpt3().respond(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
