package com.github.egubot.commands.webdriver;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.WebDriverFacade;
import com.github.egubot.interfaces.Command;

public class GrabCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return true;
	}

}
