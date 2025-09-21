package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsRollAnimationToggleCommand implements Command {

	@Override
	public String getName() {
		return "toggle roll animation";
	}

	@Override
	public String getDescription() {
		return "Toggle roll animations on or off for Dragon Ball Legends summons";
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
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
	
	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsCommandsContext.toggleIsAnimated();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}