package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.facades.LegendsTemplatesContext;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class LegendsTemplateRemove implements Command {

	@Override
	public String getName() {
		return "template remove";
	}

	@Override
	public String getDescription() {
		return "Remove a Dragon Ball Legends template from the system";
	}

	@Override
	public String getUsage() {
		return getName() + " <template_name>";
	}

	@Override
	public String getCategory() {
		return "DB Legends";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsTemplatesContext.getTemplates(msg).removeTemplate(arguments, msg.getChannel(),
				UserInfoUtilities.isOwner(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}