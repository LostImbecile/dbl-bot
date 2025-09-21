package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.facades.LegendsTemplatesContext;
import com.github.egubot.interfaces.Command;

public class LegendsTemplateSend implements Command {

	@Override
	public String getName() {
		return "template send";
	}

	@Override
	public String getDescription() {
		return "Display all available Dragon Ball Legends templates";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "DB Legends";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsTemplatesContext.getTemplates(msg).sendData(msg.getChannel());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}