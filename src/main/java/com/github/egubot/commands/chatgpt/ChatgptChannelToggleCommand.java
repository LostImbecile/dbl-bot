package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatgptChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "gpt channel toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(!AIContext.getGpt3().isAIOn())
			return false;
		
		if (AIContext.getGpt3().getActiveChannelID().equals("")) {
			AIContext.getGpt3().setActiveChannelID(msg.getChannel().getIdAsString());
		} else {
			AIContext.getGpt3().setActiveChannelID("");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
