package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsSkipCommand implements Command {

	@Override
	public String getName() {
		return "skip";
	}

	@Override
	public String getDescription() {
		return "Skip animations when rolling";
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
		
		if (msg.getChannel().getIdAsString().equals(LegendsCommandsContext.getWheelChannelID())) {
			LegendsCommandsContext.setAnimated(false);
			msg.getChannel().sendMessage("Enabled roll animation :thumbs_up:");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}


}