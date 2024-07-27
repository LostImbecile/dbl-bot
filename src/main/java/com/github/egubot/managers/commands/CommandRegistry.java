package com.github.egubot.managers.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.github.egubot.interfaces.Command;

public class CommandRegistry {
	private static final Logger logger = LogManager.getLogger(CommandRegistry.class.getName());
	private static final Map<String, Command> prefixCommandMap = new HashMap<>();
	private static final Map<String, Command> noPrefixCommandMap = new HashMap<>();

	static {
		Reflections reflections = new Reflections("com.github.egubot.commands", Scanners.SubTypes);

		// Find all classes that implement Command
		Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);

		for (Class<? extends Command> commandClass : commandClasses) {
			try {
				Command commandInstance = commandClass.getDeclaredConstructor().newInstance();
				registerCommand(commandInstance);
			} catch (Exception e) {
				logger.warn(e.getMessage());
				logger.error(e);
			}
		}

	}

	private static void registerCommand(Command command) {
		// Due to some duplicates overwriting each other
		if (command.isStartsWithPrefix())
			prefixCommandMap.put(command.getName().toLowerCase(), command);
		else
			noPrefixCommandMap.put(command.getName().toLowerCase(), command);
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