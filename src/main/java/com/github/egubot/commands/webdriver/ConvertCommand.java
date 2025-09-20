package com.github.egubot.commands.webdriver;

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
		boolean isGif = arguments.startsWith("gif") || arguments.startsWith("to vid")
				|| !(arguments.startsWith("vid") || arguments.startsWith("to gif"));
		WebDriverFacade.checkEzgifCommands(msg, isGif);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Convert gif to video and back";
	}

	@Override
	public String getUsage() {
		return getName() + "(vid/gif/to vid/to gif) <input>";
	}

	@Override
	public String getCategory() {
		return "Web Automation";
	}
}