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
