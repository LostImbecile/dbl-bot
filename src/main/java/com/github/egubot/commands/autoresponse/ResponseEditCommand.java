package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseEditCommand implements Command {

	@Override
	public String getName() {
		return "reponse edit";
	}

	@Override
	public String getDescription() {
		return "Edit an existing trigger response or attributes";
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
		AutoRespondContext.getAutoRespond(msg).updateResponse(arguments, msg.getChannel(),
				UserInfoUtilities.isPrivilegedOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}