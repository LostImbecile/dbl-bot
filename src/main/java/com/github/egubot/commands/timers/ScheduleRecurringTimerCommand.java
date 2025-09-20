package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class ScheduleRecurringTimerCommand implements Command{

	@Override
	public String getName() {
		return "timer every";
	}

	@Override
	public String getDescription() {
		return "Schedule a recurring timer that executes commands at specified intervals";
	}

	@Override
	public String getUsage() {
		return getName() + " <delay> <interval> \"<command>\"";
	}

	@Override
	public String getCategory() {
		return "Timers";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}
	
	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return ScheduledTasksContext.scheduleRecurring(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}