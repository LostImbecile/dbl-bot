package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.FixPopularSiteEmbeds;
import com.github.egubot.interfaces.Command;

public class FixEmbedCommand implements Command {

	@Override
	public String getName() {
		return "fix";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getReferencedMessage().ifPresentOrElse(
				t -> FixPopularSiteEmbeds.fixEmbed(t, t.getContent(), false, false, false),
				() -> FixPopularSiteEmbeds.fixEmbed(msg, arguments, false, false, true));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
