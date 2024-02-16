package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class ResponseEditCommand implements Command{

	@Override
	public String getName() {
		return "response edit";
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
