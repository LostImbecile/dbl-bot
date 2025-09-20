package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class TimerSendCommand implements Command {

	@Override
	public String getName() {
		return "timer send";
	}

	@Override
	public String getDescription() {
		return "Display all active timers and scheduled tasks for the server";
	}

	@Override
	public String getUsage() {
		return getName();
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
		ScheduledTasksContext.getScheduledTasks(msg).sendData(msg.getChannel());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}