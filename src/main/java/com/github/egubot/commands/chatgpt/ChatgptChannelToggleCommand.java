package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ChatGPTContext;
import com.github.egubot.interfaces.Command;

public class ChatgptChannelToggleCommand implements Command {

	@Override
	public String getName() {
		return "gpt channel toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(!ChatGPTContext.isAIOn())
			return false;
		
		if (ChatGPTContext.getActiveChannelID().equals("")) {
			ChatGPTContext.setActiveChannelID(msg.getChannel().getIdAsString());
		} else {
			ChatGPTContext.setActiveChannelID("");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
