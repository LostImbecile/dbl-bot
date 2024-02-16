package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.features.legends.LegendsSummonRates;
import com.github.egubot.interfaces.Command;

public class LegendsSummonCommand implements Command {

	@Override
	public String getName() {
		return "summon";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		String st = arguments.replace("<", "").replace(">", "").strip();
		try {
			msg.getChannel().sendMessage(LegendsSummonRates.getBannerRates(st));
		} catch (Exception e) {
			logger.error("Summon rate error.", e);
			msg.getChannel().sendMessage("Failed :thumbs_down:");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
