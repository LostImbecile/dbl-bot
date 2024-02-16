package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ResponseRemoveCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "response remove";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AutoRespondContext.getAutoRespond().removeResponse(arguments, msg.getChannel(), UserInfoUtilities.isOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
