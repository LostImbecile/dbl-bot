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
	public String getDescription() {
		return "Toggle ChatGPT always active mode on or off for specific channels";
	}

	@Override
	public String getUsage() {
		return getName() + " [#channel]";
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getChatGPT().toggleChannel(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}