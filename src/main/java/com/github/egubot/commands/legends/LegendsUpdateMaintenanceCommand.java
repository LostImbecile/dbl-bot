package com.github.egubot.commands.legends;


import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsUpdateMaintenanceCommand implements Command {
	@Override
	public String getName() {
		return "maintenance update";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if(!arguments.contains("<#")) {
			arguments += " <#" + msg.getChannel().getIdAsString() + ">";
		}
		LegendsNewsContext.updateMaintenanceServer(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Update maintenance channel";
	}

	@Override
	public String getUsage() {
		return getName() + " <channels>";
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
