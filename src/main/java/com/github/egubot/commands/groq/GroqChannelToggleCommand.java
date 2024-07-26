package com.github.egubot.commands.groq;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GroqChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "aa channel toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getLlama3().toggleChannel(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
