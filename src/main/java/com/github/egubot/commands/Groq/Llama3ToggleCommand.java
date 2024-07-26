package com.github.egubot.commands.Groq;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class Llama3ToggleCommand implements Command {

	@Override
	public String getName() {
		return "aa toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getLlama3().toggle();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
