package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsSearchCommand implements Command{

	@Override
	public String getName() {
		return "search";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		try {
			LegendsCommandsContext.getLegendsSearch().search(arguments, msg.getChannel());
		} catch (Exception e) {
			msg.getChannel().sendMessage("Filter couldn't be parsed <:huh:1184466187938185286>");
			logger.error("Legends commands error", e);
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
