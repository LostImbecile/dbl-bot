package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.WebDriverFacade;
import com.github.egubot.interfaces.Command;

public class ConvertCommand implements Command {

	@Override
	public String getName() {
		return "convert";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		WebDriverFacade.checkEzgifCommands(msg, arguments.contains("gif"), arguments.contains("vid"));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
