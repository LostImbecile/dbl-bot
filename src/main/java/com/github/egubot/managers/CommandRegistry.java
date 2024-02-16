package com.github.egubot.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.egubot.interfaces.Command;

public class CommandRegistry {
	private static final Logger logger = LogManager.getLogger(CommandRegistry.class.getName());
	private static final Map<String, Command> prefixCommandMap = new HashMap<>();
	private static final Map<String, Command> noPrefixCommandMap = new HashMap<>();

	/*
	 * If you want to add commands, go to here:
	 * resources/META-INF/services/com.github.egubot.interfaces.Command
	 * Added them as lines in that file.
	 */
	static {
		/*
		 * Automatically load commands using ServiceLoader
		 * If a command fails to load you are warned and it's skipped
		 * The bot can continue normally if missing a command
		 */
		ServiceLoader<Command> loader = ServiceLoader.load(Command.class);

		for (Iterator<Command> iterator = loader.iterator(); iterator.hasNext();) {
			try {
				Command command = iterator.next();
				registerCommand(command);
			} catch (ServiceConfigurationError e) {
				logger.warn(e.getMessage());
				logger.error(e);
			}

		}

	}

	private static void registerCommand(Command command) {
		// Due to some duplicates overwriting each other
		if (command.isStartsWithPrefix())
			prefixCommandMap.put(command.getName(), command);
		else
			noPrefixCommandMap.put(command.getName(), command);
	}

	public static Command getPrefixCommand(String commandName) {
		return prefixCommandMap.get(commandName);
	}
	
	public static Command getNoPrefixCommand(String commandName) {
		return noPrefixCommandMap.get(commandName);
	}
	
	public static Map<String, Command> getPrefixCommandmap() {
		return prefixCommandMap;
	}

	public static Map<String, Command> getNoPrefixCommandmap() {
		return noPrefixCommandMap;
	}
	
	public static void main(String[] args) {
		new CommandRegistry();
		System.out.println(CommandRegistry.getNoPrefixCommandmap().size());
		System.out.println(CommandRegistry.getPrefixCommandmap().size());
		
	}
	
}