package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRegisterNewsCommand implements Command {
	@Override
	public String getName() {
		return "news register";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		LegendsNewsContext.registerServer(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
