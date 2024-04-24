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

	public static boolean processMessage(Message msg) throws Exception {
		String text = msg.getContent();
		return processContent(msg, text);
	}

	private static boolean processContent(Message msg, String text) throws Exception {
		int prefixIndex = prefixExtractor.findPrefix(text);

		if (prefixIndex > 0) {
			// For commands with prefixes
			int commandIndex = prefixExtractor.findCommand(text);
			if (commandIndex > 0) {
				String command = text.substring(prefixIndex, commandIndex).toLowerCase();
				String arguments = text.substring(commandIndex).strip();
				return CommandRegistry.getPrefixCommand(command).execute(msg, arguments);
			}
		} else {
			// Ignore prefix, for commands without prefixes
			int commandIndex = noPrefixExtractor.findCommand(text, true);
			if (commandIndex > 0) {
				String command = text.substring(0, commandIndex).toLowerCase();
				String arguments = text.substring(commandIndex).strip();
				return CommandRegistry.getNoPrefixCommand(command).execute(msg, arguments);
			}
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		CommandManager.processContent(null, "b-response create");
	}

}
