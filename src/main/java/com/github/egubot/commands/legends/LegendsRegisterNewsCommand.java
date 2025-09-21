package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRegisterNewsCommand implements Command {
	@Override
	public String getName() {
		return "news register";
	}

	@Override
	public String getDescription() {
		return "Register to receive Dragon Ball Legends news notifications";
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
		LegendsNewsContext.registerNewsServer(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}