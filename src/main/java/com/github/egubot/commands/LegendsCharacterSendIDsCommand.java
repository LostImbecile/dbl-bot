package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class LegendsCharacterSendIDsCommand implements Command {

	@Override
	public String getName() {
		return "character printemptyids";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
