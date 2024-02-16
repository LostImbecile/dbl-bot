package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class TerminateCommand implements Command {

	@Override
	public String getName() {
		return "terminate";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		return false;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
