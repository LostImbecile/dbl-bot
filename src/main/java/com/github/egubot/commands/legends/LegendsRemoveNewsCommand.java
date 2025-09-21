package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRemoveNewsCommand implements Command {
	@Override
	public String getName() {
		return "news remove";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		LegendsNewsContext.removeNewsServer(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Remove news notifications for Dragon Ball Legends";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "DB Legends";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
}