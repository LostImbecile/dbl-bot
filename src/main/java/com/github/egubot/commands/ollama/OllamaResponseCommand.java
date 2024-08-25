package com.github.egubot.commands.ollama;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class OllamaResponseCommand implements Command {

	@Override
	public String getName() {
		return "qq";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return AIContext.getOllama().respond(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
