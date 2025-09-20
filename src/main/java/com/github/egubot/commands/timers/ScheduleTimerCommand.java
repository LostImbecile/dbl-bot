package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class ScheduleTimerCommand implements Command{

	@Override
	public String getName() {
		return "timer";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return ScheduledTasksContext.schedule(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Schedule a one-time timer that executes a command after a specified delay";
	}

	@Override
	public String getUsage() {
		return getName() + " <delay> \"<command>\"";
	}

	@Override
	public String getCategory() {
		return "Timers";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
}