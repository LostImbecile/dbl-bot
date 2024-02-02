package com.github.egubot.shared;

import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimedAction {
	public static final Logger logger = LogManager.getLogger(TimedAction.class.getName());
	private volatile boolean isTimerOn;
	private volatile boolean isRecurringTimerOn;
	private Timer timer = new Timer(true);
	private long length;
	private Date startTime;
	private Instant otherTimeFormat;
	private TimerTask singeTimerTask;
	private TimerTask recurringTimerTask;

	public TimedAction(long length, Date startTime, Instant otherTimeFormat) {
		this.length = length;
		this.isTimerOn = false;
		this.isRecurringTimerOn = false;
		this.otherTimeFormat = otherTimeFormat;
		this.startTime = startTime;
	}

	private synchronized void adjustDate(boolean adjustTillNow) {
		/*
		 * If the time has already passed, it adds the given
		 * delay to it, either once or till it's in the future
		 * but making sure it's only one length away at most
		 * from the current time.
		 */
		Instant thresholdTime = Instant.now().minusMillis(length);

		if (startTime != null && startTime.before(new Date())) {
			otherTimeFormat = startTime.toInstant();

			otherTimeFormat = otherTimeFormat.plusMillis(length);

			while (otherTimeFormat.isBefore(thresholdTime)) {
				otherTimeFormat = otherTimeFormat.plusMillis(length);
			}

			if (adjustTillNow && otherTimeFormat.isBefore(Instant.now())) {
				otherTimeFormat = otherTimeFormat.plusMillis(length);
			}

		}
		if (startTime == null && otherTimeFormat != null) {

			otherTimeFormat = otherTimeFormat.plusMillis(length);

			while (otherTimeFormat.isBefore(thresholdTime)) {
				otherTimeFormat = otherTimeFormat.plusMillis(length);
			}

			if (adjustTillNow && otherTimeFormat.isBefore(Instant.now())) {
				otherTimeFormat = otherTimeFormat.plusMillis(length);
			}

		}

		setStartTime(startTime, otherTimeFormat);
	}

	/**
	 * Schedules the given task to be performed
	 * after a set amount of times passes.
	 * 
	 * @param task TimerTask
	 */
	public synchronized void startSingleTimer(TimerTask task) {
		setSingeTimerTask(task);
		isTimerOn = true;

		timer.schedule(task, length);
	}

	/**
	 * Schedules the given task to be performed every
	 * set amount of time, starting from a date or the
	 * after the same delay.
	 * 
	 * Date can be adjusted once or till it's in the future.
	 * 
	 * @param task       TimerTask
	 * @param adjustDate boolean: If date should be adjusted
	 * @param tillNow    boolean: If adjustment is once or till the future
	 */
	public synchronized void startRecurringTimer(TimerTask task, boolean adjustDate, boolean tillNow) {
		if (adjustDate)
			adjustDate(tillNow);

		setRecurringTimerTask(task);
		isRecurringTimerOn = true;

		if (startTime != null) {
			timer.scheduleAtFixedRate(task, startTime, length);
		} else {
			timer.scheduleAtFixedRate(task, length, length);
		}

	}

	/**
	 * Starts a timer that cancels after a set amount of time.
	 * 
	 * This doesn't let another timer that's one instance
	 * only run in the meantime; unless it's recurring.
	 */
	public synchronized void startDelayTimer() {
		TimerTask updateStatusTask = new TimerTask() {

			@Override
			public void run() {
				cancelSingleTimer();
			}
		};

		startSingleTimer(updateStatusTask);
	}

	public synchronized void startOneInstanceSingleTimer(TimerTask task, int maxRetries) {
		if (isTimerOn)
			return;

		TimerTask timertask = new TimerTask() {
			@Override
			public void run() {
				isTimerOn = false;
				try {
					task.run();
				} catch (Exception e) {
					logger.error("Timer task failed to run.", e);

					if (maxRetries > 0) {
						System.out.println("Retrying failed task...");
						startOneInstanceSingleTimer(task, maxRetries);
					} else {
						System.out.println("Max retries reached. Task failed.");
					}
				}

			}
		};
		startSingleTimer(timertask);
	}

	public synchronized void startOneInstanceRecurringimer(TimerTask task, boolean adjustDate, boolean tillNow) {
		if (isRecurringTimerOn)
			return;

		TimerTask timertask = new TimerTask() {
			@Override
			public void run() {
				isRecurringTimerOn = false;
				try {
					task.run();
				} catch (Exception e) {
					logger.error("Timer task failed to run.", e);
				}
			}
		};
		startRecurringTimer(timertask, adjustDate, tillNow);
	}

	public Date getStartTime() {
		return (Date) startTime.clone();
	}

	/**
	 * Sets the start time based on a date object
	 * or an Instant, where the date object has
	 * priority.
	 * 
	 * If both are null they stay null.
	 * 
	 * @param startTime       Date: start time for recurring timers
	 * @param otherTimeFormat Instant: alternative format
	 */
	public void setStartTime(Date startTime, Instant otherTimeFormat) {
		if (startTime == null && otherTimeFormat != null) {
			this.startTime = Date.from(otherTimeFormat);
		} else {
			// Can be null here, intended
			this.startTime = startTime;
		}
	}

	public long getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public TimerTask getSingeTimerTask() {
		return singeTimerTask;
	}

	void setSingeTimerTask(TimerTask singeTimerTask) {
		this.singeTimerTask = singeTimerTask;
	}

	public TimerTask getRecurringTimerTask() {
		return recurringTimerTask;
	}

	public void setRecurringTimerTask(TimerTask recurringTimerTask) {
		this.recurringTimerTask = recurringTimerTask;
	}

	/**
	 * Cancels the latest running recurring timer
	 * task. This also stops the timer and frees
	 * it up for a new task.
	 */
	public void cancelRecurringTimer() {
		getRecurringTimerTask().cancel();
		isRecurringTimerOn = false;
	}

	/**
	 * Cancels the latest single timer
	 * task. This also stops the timer
	 * and frees it up for a new task.
	 */
	public void cancelSingleTimer() {
		getSingeTimerTask().cancel();
		isTimerOn = false;
	}

	/**
	 * Terminates timer completely, it can't be used again
	 * after this.
	 * 
	 * Timer can still be called normally and won't cause
	 * errors.
	 */
	public void terminateTimer() {
		this.timer.cancel();
	}

	public boolean isTimerOn() {
		return isTimerOn;
	}

	public boolean isRecurringTimerOn() {
		return isRecurringTimerOn;
	}

	public void setTimerOn(boolean isTimerOn) {
		this.isTimerOn = isTimerOn;
	}

	public void setRecurringTimerOn(boolean isRecurringTimerOn) {
		this.isRecurringTimerOn = isRecurringTimerOn;
	}
}
