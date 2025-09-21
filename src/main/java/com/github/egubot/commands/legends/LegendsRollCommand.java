package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRollCommand implements Command {

	@Override
	public String getName() {
		return "roll";
	}

	@Override
	public String getDescription() {
		return "Manually roll for Dragon Ball Legends characters with specified parameters";
	}

	@Override
	public String getUsage() {
		return getName() + " <count> <templates/tags>";
	}

	@Override
	public String getCategory() {
		return "DB Legends";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;

		try {
			LegendsCommandsContext.getLegendsRoll(msg).rollCharacters(arguments, msg.getChannel(),
					LegendsCommandsContext.isAnimated());
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