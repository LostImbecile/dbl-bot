package com.github.egubot.commands.chatgpt;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class ChatgptToggleCommand implements Command {

	@Override
	public String getName() {
		return "gpt toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getChatGPT().toggle();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Toggle ChatGPT functionality on or off globally";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
}