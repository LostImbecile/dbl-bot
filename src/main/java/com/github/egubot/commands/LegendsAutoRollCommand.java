package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsAutoRollCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "roll";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;

		if (msg.getChannel().getIdAsString().equals(LegendsCommandsContext.getWheelChannelID())) {
			LegendsCommandsContext.getLegendsRoll().rollCharacters("6 t1", msg.getChannel(),
					LegendsCommandsContext.isAnimated());
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return false;
	}

}
