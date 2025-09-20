package com.github.egubot.commands.groq;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.interfaces.Command;

public class GroqToggleCommand implements Command {

	@Override
	public String getName() {
		return "aa toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AIContext.getGroq().toggle();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Toggle Groq AI functionality on or off globally";
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