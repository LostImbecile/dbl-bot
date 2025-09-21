package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseCreateCommand implements Command {

	@Override
	public String getName() {
		return "response create";
	}

	@Override
	public String getDescription() {
		return "Create a new auto-response trigger and message";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Auto-Response";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!arguments.contains("sleep"))
			AutoRespondContext.getAutoRespond(msg).writeResponse(arguments, msg,
					UserInfoUtilities.isPrivilegedOwner(msg));
		else
			msg.getChannel().sendMessage("nah");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}