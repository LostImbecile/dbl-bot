package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ToggleBotReadModeCommand implements Command {

	@Override
	public String getName() {
		return "toggle bot read mode";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg))
			MessageCreateEventHandler.toggleBotReadMode();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
