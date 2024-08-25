package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class AutoDeleteCreateCommand implements Command {

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		arguments = arguments.replaceFirst("msg\\s?>>", "msg delete >>").replaceFirst("user\\s>>", "user delete >>");
		AutoRespondContext.getAutoRespond(msg).writeResponse(arguments, msg, UserInfoUtilities.isPrivilegedOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
