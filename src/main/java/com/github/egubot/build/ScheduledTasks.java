package com.github.egubot.build;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.features.TimerHandler;
import com.github.egubot.interfaces.DiscordTimerTask;
import com.github.egubot.interfaces.TimerUpdateListener;
import com.github.egubot.interfaces.UpdatableObjects;
import com.github.egubot.objects.TimerObject;
import com.github.egubot.storage.DataManagerHandler;

import java.util.ArrayList;
import java.util.Collections;

import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.ConvertObjects;
import com.github.egubot.shared.utils.JSONUtilities;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ScheduledTasks extends DataManagerHandler implements UpdatableObjects, TimerUpdateListener {
	private static final Logger logger = LogManager.getLogger(ScheduledTasks.class.getName());
	public static final String RESOURCE_PATH = "Timers.txt";
	private static String idKey = "Timers_Message_ID";
	private TimerHandler timerHandler;
	private List<TimerObject> timers;

	private static final Pattern delayPattern = Pattern.compile("(?:\\d+[smhdwM]){1,6}");
	private static final Pattern datePattern = Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{4},\\s*\\d{1,2}:\\d{2}");

	public ScheduledTasks() throws IOException {
		super(idKey, RESOURCE_PATH, "Timers", true);
		initializeTimerHandler();
	}

	public ScheduledTasks(long serverID) throws IOException {
		super(idKey, RESOURCE_PATH, "Timers", serverID, false);
		initializeTimerHandler();
	}

	private void initializeTimerHandler() {
		updateObjects();
		this.timerHandler = new TimerHandler(timers);
		updateDataFromObjects(); // if some fail to register they're removed right away
		this.timerHandler.start();
		timerHandler.addListener(this);
	}

	public boolean schedule(Message msg, String msgText, boolean isRecurring) {
		try {
			TimerObject timer = createTimer(msg, msgText, isRecurring);
			if (timer == null) {
				msg.getChannel().sendMessage("""
						Delay format: 0M0w0d0h0s
						Date format: d-m-yyyy, H:m
						Be sure to wrap the task and its arguments in quotes.""");
				return false;
			}

			if (isTimerExist(timer)) {
				msg.getChannel().sendMessage("Similar timer already exists");
			} else if (timerHandler.registerTimer(timer)) {
				msg.getChannel().sendMessage("Scheduled :ok_hand:");
				return true;
			} else {
				msg.getChannel().sendMessage("Failed to register timer");
			}
		} catch (Exception e) {
			msg.getChannel().sendMessage("Error scheduling the task.");
			logger.error(e);
		}
		return false;
	}

	public boolean remove(Message msg, String msgText) {
		try {
			boolean recurring = false;
			if (msgText.contains("recurring"))
				recurring = true;

			TimerObject timer = createTimer(msg, msgText, recurring);

			if (isTimerExist(timer)) {
				timerHandler.removeTimer(getTimer(timer));
				msg.getChannel().sendMessage("Removed :ok_hand:");
			} else
				msg.getChannel().sendMessage("Timer doesn't exist");

		} catch (Exception e) {
			msg.getChannel().sendMessage("Error removing the task.");
			logger.error(e);
		}
		return true;
	}

	public boolean isTimerExist(TimerObject timer) {
		if (timer == null)
			return false;
		boolean containsTimer = timers.contains(timer);
		if (!containsTimer) {
			timer.setRecurring(!timer.isRecurring());
			containsTimer = timers.contains(timer);
			timer.setRecurring(!timer.isRecurring());
		}
		return containsTimer;
	}

	public TimerObject getTimer(TimerObject timer) {
		if (isTimerExist(timer)) {
			int i = timers.indexOf(timer);
			if (i < 0) {
				timer.setRecurring(!timer.isRecurring());
				i = timers.indexOf(timer);
			}
			return timers.get(i);
		}

		return null;
	}

	public boolean toggleTimer(Message msg, String msgText) {
		try {
			TimerObject timer = createTimer(msg, msgText, false);
			timer = getTimer(timer);
			if (timer != null) {
				timer.setActivatedFlag(!timer.isActivatedFlag());
				updateDataFromObjects();
				writeData(msg.getChannel());
				return true;
			} else {
				msg.getChannel().sendMessage("Timer doesn't exist");
			}
		} catch (Exception e) {
			msg.getChannel().sendMessage("Error toggling timer");
			logger.error(e);
		}
		return false;
	}

	public TimerObject createTimer(Message msg, String msgText, boolean isRecurring) {
		// Split by quotes to separate the task and its arguments
		String[] parts = msgText.split("\"");
		if (parts.length < 2) {
			return null;
		}

		// Extract task and arguments
		String taskAndArgs = parts[1].trim();
		String[] before = parts[0].trim().split("(?<=[\\dA-Za-z])\\s+");

		// Extract delay and start date
		String delay = null;
		String startDate = null;
		String task = taskAndArgs.split("\\s+")[0];
		String taskArguments = taskAndArgs.substring(task.length()).trim();
		
		if(TimerHandler.getTasks().get(task) == null) {
			msg.getChannel().sendMessage("No such task");
			return null;
		}

		// Check for delay and start date in the remaining part
		for (String part : before) {
			Matcher delayMatcher = delayPattern.matcher(part);
            Matcher dateMatcher = datePattern.matcher(part);

            if (delayMatcher.matches()) {
                delay = part;
            } else if (dateMatcher.matches()) {
				startDate = part;
            }
            
		}

		// Extract channels, if present
		List<Long> channels = new ArrayList<>();
		Pattern channelPattern = Pattern.compile("<#(\\d+)>");
		if (parts.length > 2) {
			Matcher channelMatcher = channelPattern.matcher(parts[2]);
			while (channelMatcher.find()) {
				channels.add(Long.parseLong(channelMatcher.group(1)));
			}
		}
		if (channels.isEmpty()) {
			channels.add(msg.getChannel().getId());
		}

		// Create TimerObject and set fields
		TimerObject timer = new TimerObject();
		if (delay != null) {
			timer.setDelay(delay);
		}

		// Validation
		if (!isRecurring && delay == null && startDate == null) {
			if (msg != null)
				msg.getChannel().sendMessage("Either delay or start date must be provided for non-recurring tasks.");
			return null;
		}
		if (isRecurring && delay == null) {
			if (msg != null)
				msg.getChannel().sendMessage("Delay must be provided for recurring tasks.");
			return null;
		}

		timer.setTask(task);
		timer.setTaskArguments(taskArguments);
		timer.setTargetChannel(channels);
		timer.setRecurring(isRecurring);
		timer.setActivatedFlag(true);

		DateTimeFormatter outputFormatter = TimerObject.timeFormatter;
		// Determine the next execution time
		ZonedDateTime now = ZonedDateTime.now(Shared.getZoneID());
		ZonedDateTime nextExecution = now;

		if (startDate != null) {
			DateTimeFormatter dateFormatter = TimerObject.dateFormatter;
			try {
				LocalDateTime dateTime = LocalDateTime.parse(startDate, dateFormatter);
				nextExecution = dateTime.atZone(Shared.getZoneID());
				timer.setStartDate(nextExecution.format(outputFormatter));
			} catch (DateTimeParseException e) {
				if (msg != null)
					msg.getChannel().sendMessage("Invalid start date format.");
				return null;
			}
		} else if (delay != null) {
			Duration duration = timer.getDelayDuration();
			nextExecution = nextExecution.plus(duration);
		}

		boolean summerTime = now.getZone().getRules().isDaylightSavings(now.toInstant());
		timer.setSummerTime(summerTime);

		// Format the nextExecution time as a String

		String nextExecutionString = nextExecution.format(outputFormatter);

		timer.setNextExecution(nextExecutionString);

		String stripped = msgText.toLowerCase().replace("_", "").replace(" ", "");
		if (stripped.contains("terminateonmiss")) {
			timer.setTerminateOnMiss(true);
		} else if (stripped.contains("sendonmiss")) {
			timer.setSendOnMiss(true);
		} else if (isRecurring && !stripped.contains("continueonmiss")) {
			timer.setSendOnMiss(true);
		}

		return timer;
	}

	@Override
	public void shutdown() {
		timerHandler.stop();
		updateDataFromObjects();
		writeData(null);
		super.shutdown();
	}

	@Override
	public void updateObjects() {
		try {
			Gson gson = new Gson();
			String jsonData = ConvertObjects.listToText(getData());
			timers = gson.fromJson(jsonData, TimerObjectList.class).getTimers();
		} catch (JsonSyntaxException e) {
			logger.error("Syntax Error updating objects", e);
		} catch (NullPointerException e) {
			logger.error("Null pointer updating objects", e);
		}
		if (timers == null)
			timers = Collections.synchronizedList(new ArrayList<TimerObject>());
	}

	@Override
	public void updateDataFromObjects() {
		if (timers == null)
			return;
		Gson gson = new Gson();
		String jsonData = gson.toJson(new TimerObjectList(timers), TimerObjectList.class);
		jsonData = JSONUtilities.prettify(jsonData);
		setData(ConvertObjects.textToList(jsonData));
	}

	public void registerTimer(TimerObject timer) {
		timers.add(timer);
		timerHandler.registerTimer(timer);
		updateDataFromObjects();
	}

	public void removeTimer(TimerObject timer) {
		timers.remove(timer);
		timerHandler.removeTimer(timer);
		updateDataFromObjects();
	}

	public static void registerTask(DiscordTimerTask task) {
		TimerHandler.registerTask(task);
	}

	public void startTimers() {
		timerHandler.start();
	}

	public void stopTimers() {
		timerHandler.stop();
	}

	// A helper class to wrap the list of TimerObject for Gson serialization
	private static class TimerObjectList {
		private List<TimerObject> timers;

		public TimerObjectList(List<TimerObject> timers) {
			this.timers = timers;
		}

		public List<TimerObject> getTimers() {
			return Collections.synchronizedList(new ArrayList<TimerObject>(timers));
		}

	}

	@Override
	public void onTimerUpdated() {
		updateDataFromObjects();
		writeData(null);
	}

}