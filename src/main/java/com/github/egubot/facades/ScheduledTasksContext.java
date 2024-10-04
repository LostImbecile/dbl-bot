package com.github.egubot.facades;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.build.ScheduledTasks;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Shutdownable;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.utils.FileIndexer;
import com.github.egubot.storage.BaseDataManager;
import com.github.egubot.storage.DataManagerHandler;

public class ScheduledTasksContext implements Shutdownable {
	private static final Logger logger = LogManager.getLogger(ScheduledTasksContext.class.getName());
	private static Map<Long, ScheduledTasks> scheduledTasksMap = new ConcurrentHashMap<>();

	private ScheduledTasksContext() {
	}

	public static void initialise() {
		FileIndexer indexer;
		try {
			indexer = new FileIndexer(BaseDataManager.STORAGE_FOLDER);
		} catch (IOException e) {
			logger.error("Failed to create indexer.", e);
			return;
		}
		String timersFileName = "Timers" + (DataManagerHandler.isSQLite() ? ".db" : ".txt");
		List<String> timerList = indexer.getDirectoriesContainingFile(timersFileName);
		for (String timer : timerList) {
			long serverID;
			try {
				serverID = Long.parseLong(timer);
			} catch (Exception e) {
				continue;
			}
			scheduledTasksMap.computeIfAbsent(serverID, k -> {
				return new ScheduledTasks(serverID);
			});
		}
		if (!timerList.isEmpty()) {
			StreamRedirector.println("info", "\nLoaded and initialised timers for " + timerList.size() + " server(s).");
		}
	}

	public static void shutdownStatic() {
		Map<Long, ScheduledTasks> map = scheduledTasksMap;
		scheduledTasksMap = null;
		if (map == null)
			return;

		for (ScheduledTasks scheduledTasks : map.values()) {
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
			return new ScheduledTasks(serverID);
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
