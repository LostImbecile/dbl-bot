package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class RemindMeCommand implements Command {

	@Override
	public String getName() {
		return "remindme";
	}

	@Override
	public String getDescription() {
		return "Set a personal reminder for a specific time and message";
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
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!arguments.contains("\"")) {
			msg.getChannel().sendMessage("Please provide a message!");
			return true;
		}
		String authorTag = "<@" + msg.getAuthor().getIdAsString() + ">";
		String reformatted = arguments.replaceFirst("\"", "\"parrot " + authorTag + " ");
		return ScheduledTasksContext.schedule(msg, reformatted);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}