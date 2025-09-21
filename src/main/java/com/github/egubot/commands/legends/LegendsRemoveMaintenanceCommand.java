package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRemoveMaintenanceCommand implements Command {
	@Override
	public String getName() {
		return "maintenance remove";
	}

	@Override
	public String getDescription() {
		return "Remove maintenance notifications for Dragon Ball Legends";
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

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		LegendsNewsContext.removeMaintenanceServer(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}