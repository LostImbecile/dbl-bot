package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;

public class LegendsWebsiteUploadCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "website upload";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		LegendsCommandsContext.saveLegendsWebsiteBackup();
		msg.getChannel().sendMessage("Done");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
