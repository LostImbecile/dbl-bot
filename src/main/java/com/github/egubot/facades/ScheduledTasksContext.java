package com.github.egubot.facades;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.message.Message;

import com.github.egubot.build.ScheduledTasks;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;

public class ScheduledTasksContext implements Shutdownable {
	private static Map<Long, ScheduledTasks> scheduledTasksMap = new HashMap<>();

	private ScheduledTasksContext() {
	}

	public static void shutdownStatic() {
		for (ScheduledTasks scheduledTasks : scheduledTasksMap.values()) {
			if (scheduledTasks != null) {
				scheduledTasks.shutdown();
			}
		}
	}

	@Override
	public void shutdown() {
		shutdownStatic();
	}

	@Override
	public int getShutdownPriority() {
		return 0;
	}

	public static ScheduledTasks getScheduledTasks(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (serverID == -1) {
			return null;
		}
		return scheduledTasksMap.computeIfAbsent(serverID, k -> {
			try {
				return new ScheduledTasks(serverID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public static boolean schedule(Message msg, String msgText) {
		ScheduledTasks scheduledTasks = getScheduledTasks(msg);
		return scheduledTasks != null && scheduledTasks.schedule(msg, msgText, false);
	}

	public static boolean scheduleRecurring(Message msg, String msgText) {
		ScheduledTasks scheduledTasks = getScheduledTasks(msg);
		return scheduledTasks != null && scheduledTasks.schedule(msg, msgText, true);
	}

	public static boolean remove(Message msg, String msgText) {
		ScheduledTasks scheduledTasks = getScheduledTasks(msg);
		return scheduledTasks != null && scheduledTasks.remove(msg, msgText);
	}

	public static boolean toggle(Message msg, String msgText) {
		ScheduledTasks scheduledTasks = getScheduledTasks(msg);
		return scheduledTasks != null && scheduledTasks.toggleTimer(msg, msgText);
	}
}
