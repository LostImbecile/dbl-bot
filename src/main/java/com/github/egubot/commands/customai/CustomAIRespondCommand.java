package com.github.egubot.commands.customai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.interfaces.Command;

public class CustomAIRespondCommand implements Command {

	@Override
	public String getName() {
		return "ai";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(CustomAIFacade.isCustomAIOn()) {
			CustomAIFacade.getAIResponse(msg, arguments);
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
