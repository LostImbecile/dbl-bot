package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.WebDriverFacade;
import com.github.egubot.interfaces.Command;

public class InsultCommand implements Command {

	@Override
	public String getName() {
		return "insult";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		WebDriverFacade.checkInsultCommands(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
