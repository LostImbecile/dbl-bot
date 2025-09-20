package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.FixPopularSiteEmbeds;
import com.github.egubot.interfaces.Command;

public class FixEmbedToggleCommand implements Command {

	@Override
	public String getName() {
		return "fixembed toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle the fix embed feature on or off for the server";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Features";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (FixPopularSiteEmbeds.isServerDisabled(msg)) {
			FixPopularSiteEmbeds.enableServer(msg);
			msg.getChannel().sendMessage("Enabled embed fix for this server.");
		} else {
			FixPopularSiteEmbeds.disableServer(msg);
			msg.getChannel().sendMessage("Disabled embed fix for this server.");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}