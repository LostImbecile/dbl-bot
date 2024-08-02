package com.github.egubot.facades;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.build.ScheduledTasks;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.utils.FileIndexer;
import com.github.egubot.storage.DataManagerSwitcher;
import com.github.egubot.storage.LocalDataManager;

public class ScheduledTasksContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(ScheduledTasksContext.class.getName());
	private static Map<Long, ScheduledTasks> scheduledTasksMap = new HashMap<>();

	private ScheduledTasksContext() {
	}

	public static void initialise() {
		if (!DataManagerSwitcher.isOnline()) {
			FileIndexer indexer;
			try {
				indexer = new FileIndexer(LocalDataManager.STORAGE_FOLDER);
			} catch (IOException e) {
				logger.error("Failed to create indexer.", e);
				return;
			}
			List<String> timerList = indexer.getDirectoriesContainingFile(ScheduledTasks.RESOURCE_PATH);
			for (String timer : timerList) {
				long serverID;
				try {
					serverID = Long.parseLong(timer);
				} catch (Exception e) {
					continue;
				}
				scheduledTasksMap.computeIfAbsent(serverID, k -> {
					try {
						return new ScheduledTasks(serverID);
					} catch (IOException e) {
						logger.error(e);
					}
					return null;
				});
			}
			if(!timerList.isEmpty()) {
				StreamRedirector.println("info", "\nLoaded and initialised timers for " + timerList.size() + " server(s).");
			}
		}
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
				logger.error(e);
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
