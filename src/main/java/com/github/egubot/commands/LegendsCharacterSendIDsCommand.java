package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.facades.LegendsCommandsContext;
import com.github.egubot.interfaces.Command;
import com.github.egubot.objects.legends.CharacterHash;

public class LegendsCharacterSendIDsCommand implements Command {

	@Override
	public String getName() {
		return "character printemptyids";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (!LegendsCommandsContext.isLegendsMode())
			return false;
		
		CharacterHash.printEmptyIDs(LegendsDatabase.getCharacterHash());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
