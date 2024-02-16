package com.github.egubot.managers;

import java.util.Map.Entry;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class CommandManager {
	private static final CommandExtractor prefixExtractor = new CommandExtractor();
	private static final CommandExtractor noPrefixExtractor = new CommandExtractor();

	private CommandManager() {
	}

	static {
		for (Entry<String, Command> command : CommandRegistry.getPrefixCommandmap().entrySet()) {
			prefixExtractor.insertCommand(command.getKey());

		}

		for (Entry<String, Command> command : CommandRegistry.getNoPrefixCommandmap().entrySet()) {
			noPrefixExtractor.insertCommand(command.getKey());

		}
	}

	public static boolean processMessage(Message msg) {
		String text = msg.getContent();
		int index;
		if (prefixExtractor.findPrefix(text) > 0) {
			// For commands with prefixes
			index = prefixExtractor.findCommand(text);
			if (index > 0) {
				return CommandRegistry.getPrefixCommand(text.substring(0, index)).execute(msg, text.substring(index));
			}
		} else {
			// Ignore prefix, for commands without prefixes
			index = noPrefixExtractor.findCommand(text, true);
			if (index > 0) {
				return CommandRegistry.getNoPrefixCommand(text.substring(0, index)).execute(msg, text.substring(index));
			}
		}

		return false;
	}

}
