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
	public String getDescription() {
		return "Toggle Groq AI always active mode on or off for specific channels";
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
		AIContext.getGroq().toggleChannel(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}