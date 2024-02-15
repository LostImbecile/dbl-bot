package com.github.egubot.managers;

import java.util.Map.Entry;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class CommandManager {
	private static final CommandExtractor extractor = new CommandExtractor();

	private CommandManager() {
	}
	
	static {
		for (Entry<String, Command> command : CommandRegistry.getCommandMap().entrySet()) {
			extractor.insertCommand(command.getKey());
		}
	}

	public static boolean processMessage(Message msg) {
		String text = msg.getContent();
		int index;
		if (extractor.findPrefix(text) > 0) {
			index = extractor.findCommand(text);
		} else {
			// Ignore prefix, for commands without prefixes
			index = extractor.findCommand(text, true);
		}
		if (index > 0) {
			return CommandRegistry.get(text.substring(0, index)).execute(msg, text.substring(index));
		}
		return false;
	}
}
