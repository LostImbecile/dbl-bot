package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ToggleOwnMessageReadModeCommand implements Command {

	@Override
	public String getName() {
		return "toggle own message read mode";
	}

	@Override
	public String getDescription() {
		return "Toggle whether the bot processes messages from itself";
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
			MessageCreateEventHandler.toggleOwnReadMode();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}