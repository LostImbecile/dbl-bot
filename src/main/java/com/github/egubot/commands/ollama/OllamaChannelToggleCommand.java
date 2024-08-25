package com.github.egubot.commands.ollama;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class OllamaChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "qq channel toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getOllama().toggleChannel(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
