package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseSendCommand implements Command {

	@Override
	public String getName() {
		return "responses";
	}

	@Override
	public String getDescription() {
		return "Display all auto-responses";
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
		if (UserInfoUtilities.isPrivilegedOwner(msg))
			AutoRespondContext.getAutoRespond(msg).sendData(msg.getChannel());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}