package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.facades.LegendsTemplatesContext;
import com.github.egubot.interfaces.Command;

public class LegendsTemplateCreate implements Command {

	@Override
	public String getName() {
		return "template create";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsTemplatesContext.getTemplates(msg).writeTemplate(arguments, msg.getChannel());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
