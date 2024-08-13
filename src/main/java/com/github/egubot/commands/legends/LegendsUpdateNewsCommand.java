package com.github.egubot.commands.legends;


import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsNewsContext;
import com.github.egubot.interfaces.Command;

public class LegendsUpdateNewsCommand implements Command {
	@Override
	public String getName() {
		return "news update";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if(!arguments.contains("<#")) {
			arguments += " <#" + msg.getChannel().getIdAsString() + ">";
		}
		LegendsNewsContext.updateNewsServer(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
