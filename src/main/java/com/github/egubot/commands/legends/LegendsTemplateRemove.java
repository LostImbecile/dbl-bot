package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.facades.LegendsTemplatesContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class LegendsTemplateRemove implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "template remove";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsTemplatesContext.getTemplates().removeTemplate(arguments, msg.getChannel(),
				UserInfoUtilities.isOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
