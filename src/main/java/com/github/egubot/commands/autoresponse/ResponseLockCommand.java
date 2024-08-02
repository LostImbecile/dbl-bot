package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseLockCommand implements Command {

	@Override
	public String getName() {
		return "response lock";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isPrivilegedOwner(msg)) {
			int x = Integer.parseInt(arguments.replaceAll("\\D", ""));
			AutoRespondContext.getAutoRespond(msg).setLockedDataEndIndex(x);
			AutoRespondContext.getAutoRespond(msg).writeData(msg.getChannel());
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
