package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRemoveNewsCommand implements Command {
	@Override
	public String getName() {
		return "news remove";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		LegendsNewsContext.removeNewsServer(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
