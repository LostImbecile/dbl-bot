package com.github.egubot.commands.legends;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.SendObjects;

public class LegendsCharacterSendCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "character send";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		SendObjects.sendCharacters(msg.getChannel(), LegendsDatabase.getCharactersList());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
