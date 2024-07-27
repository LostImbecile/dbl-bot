package com.github.egubot.features;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import com.github.egubot.objects.TimerObject;
import com.github.egubot.shared.Shared;

public class TimerHandler {
	private static final Logger logger = LogManager.getLogger(TimerHandler.class.getName());
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private static final Map<String, DiscordTimerTask> tasks = new HashMap<>();
	private List<TimerObject> timers;
	private final Map<TimerObject, ScheduledFuture<?>> scheduledFutures = new HashMap<>();

	public TimerHandler(List<TimerObject> timers) {
		if (timers != null) {
			this.timers = timers;
			for (int i = 0; i < timers.size(); i++) {
				TimerObject timer = timers.get(i);
				if (!isValid(timer)) {
					i--;
				}
			}
		}
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

	private boolean isValid(TimerObject timer) {
		try {
			formatTimeString(timer.getNextExecutionTime());
			return true;
		} catch (Exception e) {
			logger.error("Failed to register timer", e);
			timers.remove(timer);
		}
		return false;
	}

	public void registerTask(DiscordTimerTask task) {
		tasks.put(task.getName(), task);
	}

	public void removeTask(TimerObject timer) {
		int i = timers.indexOf(timer);
		// Make sure the timer doesn't keep re-scheduling even past cancelling
		timers.get(i).setActivatedFlag(false);
		timers.remove(i);
		ScheduledFuture<?> future = scheduledFutures.remove(timer);
		if (future != null) {
			future.cancel(false); // Attempt to cancel the scheduled task
		}
		logger.debug("Removed timer {} meant for {}", timer.getTask(), timer.getNextExecutionTime());
	}

	public boolean registerTimer(TimerObject timer) {
		try {
			formatTimeString(timer.getNextExecutionTime());
			logger.debug("Registered timer {} with next execution time {}", timer.getTask(),
					timer.getNextExecutionTime());
			timers.add(timer);
			scheduleTimer(timer); // Schedule the timer immediately upon registration
			return true;
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
	}

	public void stop() {
		scheduler.shutdown();
		String exitTime = ZonedDateTime.now(ZoneId.of(Shared.getTimeZone())).format(TimerObject.timeFormatter);
		for (TimerObject timerObject : timers) {
			timerObject.setExitTime(exitTime);
		}
		// Cancel all scheduled tasks
		for (ScheduledFuture<?> future : scheduledFutures.values()) {
			future.cancel(false);
		}
		scheduledFutures.clear();
	}

	private void scheduleTimer(TimerObject timer) {
		Duration delay;
		if (timer.getStartDateTime() != null) {
			delay = Duration.between(LocalDateTime.now(ZoneId.of(Shared.getTimeZone())), timer.getStartDateTime());
			if (delay.isNegative()) {
				delay = Duration.ZERO;
			}
		} else {
			delay = Duration.between(LocalDateTime.now(ZoneId.of(Shared.getTimeZone())), timer.getNextExecutionTime());
		}
		ScheduledFuture<?> future = scheduler.schedule(() -> handleTimerExecution(timer), delay.toMillis(),
				TimeUnit.MILLISECONDS);

		scheduledFutures.put(timer, future);
	}

	private void handleTimerExecution(TimerObject timer) {
		if (!timer.isActivatedFlag())
			return;
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Shared.getTimeZone()));
		ZonedDateTime nextExecutionTime = timer.getNextExecutionTime();
		Duration missTolerance = timer.getMissToleranceDuration();

		logger.debug("Executing timer {} with next execution time {}", timer.getTask(), nextExecutionTime);
		// Adjust the next execution time by adding miss tolerance
		ZonedDateTime adjustedNextExecutionTime = nextExecutionTime.plus(missTolerance);

		updateNextExecution(timer);

		if (now.isAfter(adjustedNextExecutionTime) && !timer.isSendOnMiss()) {
			if (timer.isTerminateOnMiss()) {
				removeTask(timer);
				return;
			}

			if (timer.isContinueOnMiss()) {
				scheduleTimer(timer);
				logger.debug("Next execution time for timer {} is {}", timer.getTask(), timer.getNextExecutionTime());
				return;
			}

		}

		executeTask(timer);

		if (timer.isRecurring()) {
			scheduleTimer(timer); // Reschedule with updated nextExecutionTime
			logger.debug("Next execution time for timer {} is {}", timer.getTask(), timer.getNextExecutionTime());
		} else {
			removeTask(timer);
		}

	}

	private void updateNextExecution(TimerObject timer) {
		ZonedDateTime nextExecution = timer.getNextExecutionTime();
		Duration delay = timer.getDelayDuration();
		// Keep adding delay to nextExecutionTime until it is after the current time
		while (nextExecution.isBefore(ZonedDateTime.now(ZoneId.of(Shared.getTimeZone())))) {
			nextExecution = nextExecution.plus(delay);
		}
		timer.setNextExecution(formatTimeString(nextExecution.plus(delay)));
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
}
