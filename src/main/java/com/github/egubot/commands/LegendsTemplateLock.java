package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.facades.LegendsTemplatesContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class LegendsTemplateLock implements Command {

	@Override
	public String getName() {
		return "template lock";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		if (UserInfoUtilities.isOwner(msg)) {
			int x = Integer.parseInt(arguments.replaceAll("\\D", ""));
			LegendsTemplatesContext.getTemplates().setLockedDataEndIndex(x);
			LegendsTemplatesContext.getTemplates().writeData(msg.getChannel());
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
