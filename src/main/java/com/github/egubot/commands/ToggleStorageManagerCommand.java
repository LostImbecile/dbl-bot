package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class ToggleStorageManagerCommand implements Command {

	@Override
	public String getName() {
		return "toggle manager";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		return false;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
