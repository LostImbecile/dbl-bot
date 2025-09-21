package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseRemoveCommand implements Command {

	@Override
	public String getName() {
		return "response remove";
	}

	@Override
	public String getDescription() {
		return "Remove an existing auto-response trigger from the current channel";
	}

	@Override
	public String getUsage() {
		return getName() + " <trigger>";
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
		AutoRespondContext.getAutoRespond(msg).removeResponse(arguments, msg.getChannel(), UserInfoUtilities.isPrivilegedOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}