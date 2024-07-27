package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class ToggleTimerCommand implements Command {

	@Override
	public String getName() {
		return "timer toggle";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		return ScheduledTasksContext.toggle(msg, arguments);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
