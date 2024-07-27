package com.github.egubot.facades;

import java.io.IOException;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.ScheduledTasks;
import com.github.egubot.interfaces.Shutdownable;

public class ScheduledTasksContext implements Shutdownable {
	private static ScheduledTasks scheduledTasks = null;

	private ScheduledTasksContext() {
	}

	public static void initialise() throws IOException {
		scheduledTasks = new ScheduledTasks();
	}

	public static void shutdownStatic() {
		if (scheduledTasks != null)
			scheduledTasks.shutdown();
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static ScheduledTasks getScheduledTasks() {
		return scheduledTasks;
	}

	public static boolean schedule(Message msg, String msgText) {
		return scheduledTasks.schedule(msg, msgText, false);
	}
	
	public static boolean scheduleRecurring(Message msg, String msgText) {
		return scheduledTasks.schedule(msg, msgText, true);
	}
	
	public static boolean remove(Message msg, String msgText) {
		return scheduledTasks.remove(msg, msgText);
	}
	
	public static boolean toggle(Message msg, String msgText) {
		return scheduledTasks.toggleTimer(msg, msgText);
	}
}
