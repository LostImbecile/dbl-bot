package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRegisterMaintenanceCommand implements Command {
	@Override
	public String getName() {
		return "maintenance register";
	}

	@Override
	public String getDescription() {
		return "Register a new maintenance notification for Dragon Ball Legends";
	}

	@Override
	public String getUsage() {
		return getName() + " <channel>";
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
		if (!arguments.contains("<#")) {
			arguments += " <#" + msg.getChannel().getIdAsString() + ">";
		}
		LegendsNewsContext.registerMaintenanceServer(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}