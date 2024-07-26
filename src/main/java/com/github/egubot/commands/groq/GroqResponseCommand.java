package com.github.egubot.commands.groq;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GroqResponseCommand implements Command {

	@Override
	public String getName() {
		return "aa";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return AIContext.getLlama3().respond(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
