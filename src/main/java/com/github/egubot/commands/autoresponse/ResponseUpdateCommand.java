package com.github.egubot.commands.autoresponse;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AutoRespondContext;
import com.github.egubot.interfaces.Command;

public class ResponseUpdateCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "response update";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		AutoRespondContext.getAutoRespond().writeDataNow(msg.getChannel());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
