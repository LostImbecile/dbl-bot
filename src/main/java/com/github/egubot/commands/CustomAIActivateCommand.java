package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class CustomAIActivateCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ai activate";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return false;
	}

}
