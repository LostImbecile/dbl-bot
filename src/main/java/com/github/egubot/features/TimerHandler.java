package com.github.egubot.features;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.github.egubot.interfaces.DiscordTimerTask;
import com.github.egubot.interfaces.TimerUpdateListener;
import com.github.egubot.objects.TimerObject;
import com.github.egubot.shared.Shared;
import com.github.egubot.shared.utils.DateUtils;

public class TimerHandler {
	private static final Logger logger = LogManager.getLogger(TimerHandler.class.getName());
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
	private static final Map<String, DiscordTimerTask> tasks = new HashMap<>();
	private List<TimerObject> timers;
	private final Map<TimerObject, ScheduledFuture<?>> scheduledFutures = new HashMap<>();
	private List<TimerUpdateListener> listeners = new ArrayList<>(1);
	private ZonedDateTime lastCheckTime;

	static {
		Reflections reflections = new Reflections("com.github.egubot", Scanners.SubTypes);

		// Find all classes that implement DiscordTimerTask
		Set<Class<? extends DiscordTimerTask>> taskClasses = reflections.getSubTypesOf(DiscordTimerTask.class);

		for (Class<? extends DiscordTimerTask> taskClass : taskClasses) {
			try {
				DiscordTimerTask taskInstance = taskClass.getDeclaredConstructor().newInstance();
				registerTask(taskInstance);
			} catch (Exception e) {
				logger.error("Failed to register task: {}", taskClass.getName(), e);
			}
		}
	}

	public TimerHandler(List<TimerObject> timers) {
		if (timers != null) {
			this.timers = timers;
			for (int i = 0; i < timers.size(); i++) {
				TimerObject timer = timers.get(i);
				if (!isValid(timer)) {
					timers.remove(i);
					i--;
				}
			}
		}
	}

	private boolean isValid(TimerObject timer) {
		try {
			if (timer != null) {
				formatTimeString(timer.getNextExecutionTime());
				return true;
			}
		} catch (Exception e) {
			logger.error("Failed to register timer", e);
		}
		return false;
	}

	public static void registerTask(DiscordTimerTask task) {
		tasks.put(task.getName().replace(" ", "_"), task);
	}

	public void removeTimer(TimerObject timer) {
		int i = timers.indexOf(timer);
		// Make sure the timer doesn't keep re-scheduling even past cancelling
		if (i != -1) {
			timers.get(i).setActivatedFlag(false);
			timers.remove(i);
			removeFuture(timer);
			logger.debug("Removed timer {} meant for {}", timer.getTask(), timer.getNextExecutionTime());
			notifyListeners();
		}
	}

	public void removeFuture(TimerObject timer) {
		ScheduledFuture<?> future = scheduledFutures.remove(timer);
		if (future != null) {
			future.cancel(false); // Attempt to cancel the scheduled task
		}
	}

	public boolean registerTimer(TimerObject timer) {
		try {
			if (isValid(timer)) {
				logger.debug("Registered timer {} with next execution time {}", timer.getTask(),
						timer.getNextExecutionTime());
				timers.add(timer);
				scheduleTimer(timer); // Schedule the timer immediately upon registration
				return true;
			}
		} catch (Exception e) {
			logger.error("Failed to register timer", e);
		}
		return false;
	}

	public void start() {
		// Initial scheduling of all timers
		for (TimerObject timer : timers) {
			scheduleTimer(timer);
		}
		lastCheckTime = getNow();
		startSystemTimeCheck();
	}

	public void stop() {
		scheduler.shutdown();
		// Cancel all scheduled tasks
		for (ScheduledFuture<?> future : scheduledFutures.values()) {
			future.cancel(false);
		}
		scheduledFutures.clear();

		// Set exit time for all timers
		String exitTime = getNow().format(TimerObject.timeFormatter);
		for (TimerObject timerObject : timers) {
			timerObject.adjustTimesForSummerTime();
			timerObject.setExitTime(exitTime);
		}

	}

	private void scheduleTimer(TimerObject timer) {
		if (!timer.isActivatedFlag())
			return;

		removeFuture(timer);

		timer.adjustTimesForSummerTime();
		Duration delay;

		if (timer.getStartDateTime() != null) {
			delay = Duration.between(getNow(), timer.getStartDateTime());
		} else {
			delay = Duration.between(getNow(), timer.getNextExecutionTime());
			// Check if the timer is to continue on miss
			if (timer.isContinueOnMiss() && timer.getExitTimeAsDateTime() != null) {
				// Calculate the delay from the next execution time
				ZonedDateTime nextExecutionTime = timer.getNextExecutionTime();
				Duration missTolerance = timer.getMissToleranceDuration();
				ZonedDateTime adjustedNextExecutionTime = nextExecutionTime.plus(missTolerance);
				ZonedDateTime now = getNow();

				if (now.isAfter(adjustedNextExecutionTime)) {
					ZonedDateTime exitTime = timer.getExitTimeAsDateTime();
					Duration remainingDelay = Duration.between(exitTime, nextExecutionTime);
					delay = remainingDelay;

					// Reset exit time
					timer.setExitTime(null);
					// Update next execution time so the task continues normally
					timer.setNextExecution(nextExecutionTime.plus(remainingDelay).format(TimerObject.timeFormatter));
				}
			}
		}
		if (delay.isNegative()) {
			delay = Duration.ZERO;
			// Don't update next execution
		}

		logger.debug("Next execution time for timer {} is {}", timer.getTask(), timer.getNextExecutionTime());

		ScheduledFuture<?> future = scheduler.schedule(() -> handleTimerExecution(timer), delay.toMillis(),
				TimeUnit.MILLISECONDS);
		scheduledFutures.put(timer, future);
		notifyListeners();
	}

	public static ZonedDateTime getNow() {
		return ZonedDateTime.now(Shared.getZoneID());
	}

	private void handleTimerExecution(TimerObject timer) {
		if (!timer.isActivatedFlag())
			return;
		ZonedDateTime now = getNow();
		ZonedDateTime nextExecutionTime = timer.getNextExecutionTime();
		Duration missTolerance = timer.getMissToleranceDuration();

		// Adjust the next execution time by adding miss tolerance
		ZonedDateTime adjustedNextExecutionTime = nextExecutionTime.plus(missTolerance);

		if (now.isAfter(adjustedNextExecutionTime) && !timer.isSendOnMiss()) {
			if (timer.isTerminateOnMiss() && !timer.isRecurring()) {
				removeTimer(timer);
				return;
			}

			scheduleNext(timer);
			return;

		}

		executeTask(timer);

		if (timer.isRecurring()) {
			scheduleNext(timer);
		} else {
			removeTimer(timer);
		}

	}

	public void scheduleNext(TimerObject timer) {
		// Start using delay next
		timer.setStartDate(null);
		updateNextExecution(timer);
		scheduleTimer(timer); // Reschedule with updated nextExecutionTime
	}

	private void updateNextExecution(TimerObject timer) {
		ZonedDateTime nextExecution = timer.getNextExecutionTime();
		Duration delay = timer.getDelayDuration();
		// Keep adding delay to nextExecutionTime until it is after the current time
		while (nextExecution.isBefore(getNow())) {
			nextExecution = nextExecution.plus(delay);
		}
		timer.setNextExecution(formatTimeString(nextExecution));
	}

	private void executeTask(TimerObject timer) {
		for (Long channelId : timer.getTargetChannel()) {
			try {
				tasks.get(timer.getTask()).execute(channelId, timer.getTaskArguments());
			} catch (Exception e) {
				logger.error("Failed to execute task for timer: {}", timer.getTask(), e);
			}
		}
	}

	private String formatTimeString(ZonedDateTime zonedDateTime) {
		DateTimeFormatter dateFormatter = TimerObject.timeFormatter;
		return zonedDateTime.format(dateFormatter);
	}

	private void startSystemTimeCheck() {
		scheduler.scheduleAtFixedRate(() -> {
			ZonedDateTime now = getNow();
			Duration timeDifference = Duration.between(lastCheckTime, now).minus(Duration.ofSeconds(5));

			if (Math.abs(timeDifference.toMinutes()) > 1) {
				logger.debug("System time changed by {}", DateUtils.formatDurationAsDelay(timeDifference));
				adjustTimers();
			}

			lastCheckTime = now;
		}, 0, 1, TimeUnit.MINUTES);
	}

	private void adjustTimers() {
		List<TimerObject> timersCopy = new ArrayList<>(this.timers);
		for (TimerObject timer : timersCopy) {
			// Also cancels the current timer task before rescheduling
			scheduleTimer(timer);
		}
	}

	public void addListener(TimerUpdateListener listener) {
		listeners.add(listener);
	}

	public void removeListener(TimerUpdateListener listener) {
		listeners.remove(listener);
	}

	public void notifyListeners() {
		for (TimerUpdateListener listener : listeners) {
			listener.onTimerUpdated();
		}
	}

	public static Map<String, DiscordTimerTask> getTasks() {
		return tasks;
	}
}
