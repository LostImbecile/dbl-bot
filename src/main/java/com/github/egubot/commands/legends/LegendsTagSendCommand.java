package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.SendObjects;

public class LegendsTagSendCommand implements Command {

	@Override
	public String getName() {
		return "tag send";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		SendObjects.sendTags(msg.getChannel(), LegendsDatabase.getTags());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
