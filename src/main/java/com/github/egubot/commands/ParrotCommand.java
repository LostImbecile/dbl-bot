package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ParrotCommand implements Command {

	@Override
	public String getName() {
		return "parrot";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg))
			msg.getChannel().sendMessage(arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return false;
	}

}
