package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.FileUtilities;

public class HelpCommand implements Command {

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		msg.getChannel().sendMessage(FileUtilities.getFileInputStream("commands", false), "commands.txt");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
