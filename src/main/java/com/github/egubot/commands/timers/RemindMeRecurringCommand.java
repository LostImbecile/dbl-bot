package com.github.egubot.commands.timers;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.ScheduledTasksContext;
import com.github.egubot.interfaces.Command;

public class RemindMeRecurringCommand implements Command{

	@Override
	public String getName() {
		return "remindme every";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!arguments.contains("\"")) {
			msg.getChannel().sendMessage("Please provide a message!");
			return true;
		}
		String authorTag = "<@" + msg.getAuthor().getIdAsString() + ">";
		String reformatted = arguments.replaceFirst("\"", "\"parrot " + authorTag + " ");
		return ScheduledTasksContext.scheduleRecurring(msg, reformatted);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
