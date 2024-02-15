package com.github.egubot.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.github.egubot.interfaces.Command;

public class CommandRegistry {
    private static final Map<String, Command> commandMap = new HashMap<>();

	/*
	 * If you want to add commands, go to here:
	 * resources/META-INF/services/com.github.egubot.interfaces.Command
	 * Added them as lines in that file.
	 */
    static {
        // Automatically load commands using ServiceLoader
        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        for (Command command : loader) {
            registerCommand(command);
        }
    }

    private static void registerCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    public static Command get(String commandName) {
        return commandMap.get(commandName);
    }

	public static Map<String, Command> getCommandMap() {
		return commandMap;
	}
    
}