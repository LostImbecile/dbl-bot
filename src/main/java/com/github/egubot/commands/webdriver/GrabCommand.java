package com.github.egubot.commands.webdriver;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.WebDriverFacade;
import com.github.egubot.interfaces.Command;

public class GrabCommand implements Command {

	@Override
	public String getName() {
		return "grab";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		msg.getChannel().sendMessage("one moment");
		WebDriverFacade.checkGrabCommands(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Grab youtube video link for download";
	}

	@Override
	public String getUsage() {
		return getName() + " <url>";
	}

	@Override
	public String getCategory() {
		return "Web Automation";
	}
}