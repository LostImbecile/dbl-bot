package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsAutoRollCommand implements Command {

	// this is without prefix for reference
	@Override
	public String getName() {
		return "roll";
	}

	@Override
	public String getDescription() {
		return "Roll for random Dragon Ball Legends characters";
	}

	@Override
	public String getUsage() {
		return getName() + "<count> <template/tags>";
	}

	@Override
	public String getCategory() {
		return "DB Legends";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;

		if (msg.getChannel().getIdAsString().equals(LegendsCommandsContext.getWheelChannelID())) {
			LegendsCommandsContext.getLegendsRoll(msg).rollCharacters("6 t1", msg.getChannel(),
					LegendsCommandsContext.isAnimated());
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}