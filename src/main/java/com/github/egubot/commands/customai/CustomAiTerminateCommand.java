package com.github.egubot.commands.customai;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.CustomAIFacade;
import com.github.egubot.interfaces.Command;

public class CustomAiTerminateCommand implements Command {

	@Override
	public String getName() {
		return "ai terminate";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		CustomAIFacade.setCustomAIOn(false);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
