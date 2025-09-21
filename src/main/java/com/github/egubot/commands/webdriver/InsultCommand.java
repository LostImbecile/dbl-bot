package com.github.egubot.commands.webdriver;

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

	@Override
	public String getDescription() {
		return "Generate creative insults using web-based content generation";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Fun";
	}
}