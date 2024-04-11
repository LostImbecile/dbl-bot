package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.interfaces.Command;

public class TestToggleCommand implements Command {

	@Override
	public String getName() {
		return "test toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		MessageCreateEventHandler.toggleTestClass();
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
