package com.github.egubot.commands.customai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.interfaces.Command;

public class CustomAIActivateCommand implements Command {

	@Override
	public String getName() {
		return "ai activate";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		CustomAIFacade.setCustomAIOn(true);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
