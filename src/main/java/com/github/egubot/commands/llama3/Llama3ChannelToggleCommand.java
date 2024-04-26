package com.github.egubot.commands.llama3;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class Llama3ChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "aa channel toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (AIContext.getLlama3().getActiveChannelID().equals("")) {
			AIContext.getLlama3().setActiveChannelID(msg.getChannel().getIdAsString());
		} else {
			AIContext.getLlama3().setActiveChannelID("");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
