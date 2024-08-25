package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class AutoDeleteRemoveCommand implements Command {

	@Override
	public String getName() {
		return "delete remove";
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
