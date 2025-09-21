package com.github.egubot.commands.botcontrol;

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
	public String getDescription() {
		return "Toggle whether the bot reads and processes bot messages";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Bot Control";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OWNER;
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