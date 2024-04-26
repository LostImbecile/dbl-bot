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
		AIContext.getLlama3().toggleChannel(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
