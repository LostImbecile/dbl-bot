package com.github.egubot.features;

import java.time.Duration;
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
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
	private static final Map<String, DiscordTimerTask> tasks = new HashMap<>();
	private List<TimerObject> timers;
	private final Map<TimerObject, ScheduledFuture<?>> scheduledFutures = new HashMap<>();
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
					i--;
				}
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

	public static void registerTask(DiscordTimerTask task) {
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
		timer.adjustTimesForSummerTime();
		Duration delay;

		if (timer.getStartDateTime() != null) {
			delay = Duration.between(getNow(), timer.getStartDateTime());
			// It starts using delay next if recurring
			timer.setStartDate(null);
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
					delay = Duration.between(getNow(), getNow().plus(remainingDelay));

					// Reset exit time
					timer.setExitTime(null);
					timer.setNextExecution(nextExecutionTime.plus(remainingDelay).format(TimerObject.timeFormatter));
				}
			}
		}
		if (delay.isNegative()) {
			delay = Duration.ZERO;
		}
		
		logger.debug("Next execution time for timer {} is {}", timer.getTask(), timer.getNextExecutionTime());

		ScheduledFuture<?> future = scheduler.schedule(() -> handleTimerExecution(timer), delay.toMillis(),
				TimeUnit.MILLISECONDS);
		scheduledFutures.put(timer, future);
	}

	public ZonedDateTime getNow() {
		return ZonedDateTime.now(ZoneId.of(Shared.getTimeZone()));
	}

	private void handleTimerExecution(TimerObject timer) {
		if (!timer.isActivatedFlag())
			return;
		ZonedDateTime now = getNow();
		ZonedDateTime nextExecutionTime = timer.getNextExecutionTime();
		Duration missTolerance = timer.getMissToleranceDuration();

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
				return;
			}

		}

		executeTask(timer);

		if (timer.isRecurring()) {
			scheduleTimer(timer); // Reschedule with updated nextExecutionTime
		} else {
			removeTask(timer);
		}

	}

	private void updateNextExecution(TimerObject timer) {
		ZonedDateTime nextExecution = timer.getNextExecutionTime();
		Duration delay = timer.getDelayDuration();
		// Keep adding delay to nextExecutionTime until it is after the current time
		while (nextExecution.isBefore(getNow())) {
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

	private void startSystemTimeCheck() {
		scheduler.scheduleAtFixedRate(() -> {
			ZonedDateTime now = getNow();
			Duration timeDifference = Duration.between(lastCheckTime, now).minus(Duration.ofSeconds(5));

			if (Math.abs(timeDifference.toMinutes()) > 1) {
				adjustTimers(timeDifference);
			}

			lastCheckTime = now;
		}, 0, 1, TimeUnit.MINUTES);
	}

	private void adjustTimers(Duration timeDifference) {
		for (TimerObject timer : timers) {
			timer.adjustTimesForSummerTime();
			ScheduledFuture<?> future = scheduledFutures.get(timer);
			if (future != null) {
				future.cancel(false);
				Duration newDelay = Duration.between(getNow(), timer.getNextExecutionTime()).minus(timeDifference);
				if (newDelay.isNegative()) {
					newDelay = Duration.ZERO;
				}
				ScheduledFuture<?> newFuture = scheduler.schedule(() -> handleTimerExecution(timer),
						newDelay.toMillis(), TimeUnit.MILLISECONDS);
				scheduledFutures.put(timer, newFuture);
				logger.info("Adjusted timer {} with new delay of {}", timer.getTask(), newDelay);
			}
		}
	}
}
