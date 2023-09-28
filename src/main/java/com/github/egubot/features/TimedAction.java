package com.github.egubot.features;

import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.javacord.api.entity.message.Messageable;

/*
 * This class has bot specific implementations, while
 * it could be used for other things (basic timers mainly)
 * you'll likely need to change it to suit your needs first.
 * 
 * Important methods are documented, the rest are either
 * straightforward or implementation specific.
 */
public class TimedAction {
	private Timer timer = new Timer(true);
	private long length;
	private Date startTime;
	private Instant otherTimeFormat;
	private boolean isTimerOn;
	private boolean isRecurringTimerOn;
	private TimerTask singeTimerTask;
	private TimerTask recurringTimerTask;

	public TimedAction(long length, Date startTime, Instant otherTimeFormat) {
		this.length = length;
		this.isTimerOn = false;
		this.isRecurringTimerOn = false;
		this.otherTimeFormat = otherTimeFormat;
		this.startTime = startTime;
	}

	private void adjustDate(boolean adjustTillNow) {
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
	public void startSingleTimer(TimerTask task) {
		setSingeTimerTask(task);
		isTimerOn = true;

		try {
			timer.schedule(task, length);
		} catch (Exception e) {

		}
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
	public void startRecurringTimer(TimerTask task, boolean adjustDate, boolean tillNow) {
		if (adjustDate)
			adjustDate(tillNow);

		// System.out.println("Recurring timer started");
		setRecurringTimerTask(task);
		isRecurringTimerOn = true;

		try {
			if (startTime != null) {
				// System.out.println("Scheduled for: " + startTime);
				timer.scheduleAtFixedRate(task, startTime, length);
			} else {
				// System.out.println("Active in " + length + " hours");
				timer.scheduleAtFixedRate(task, length, length);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Starts a timer that cancels after a set amount of time.
	 * 
	 * This doesn't let another timer that's one instance
	 * only run in the meantime; unless it's recurring.
	 */
	public void startDelayTimer() {
		TimerTask updateStatusTask = new TimerTask() {

			@Override
			public void run() {
				cancelSingleTimer();
			}
		};

		startSingleTimer(updateStatusTask);
	}

	public void sendDelayedMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Sends a message after a set amount of time
		if (isOneInstanceOnly && isTimerOn || e == null) {
			return;
		}

		TimerTask sendTask = new TimerTask() {

			@Override
			public void run() {
				e.sendMessage(text);
				cancelSingleTimer();
			}
		};

		startSingleTimer(sendTask);
	}

	public void sendScheduledMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Schedules a message to be sent every set amount of time
		// Date is adjusted once only in this.
		if (isOneInstanceOnly && isRecurringTimerOn || e == null) {
			return;
		}

		// System.out.println("Message is getting scheduled");
		TimerTask sendTask = new TimerTask() {

			@Override
			public void run() {
				e.sendMessage(text);
			}
		};

		startRecurringTimer(sendTask, true, false);
	}

	public void sendDelayedRateLimitedMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Sends a message after a set amount of time
		// or a date, where date is not adjusted.
		if (isOneInstanceOnly && isTimerOn || e == null) {
			return;
		}

		startDelayTimer();
		TimerTask delayedStartTask = new TimerTask() {

			@Override
			public void run() {
				e.sendMessage(text).join();
				cancelRecurringTimer();
			}
		};

		startRecurringTimer(delayedStartTask, true, false);
	}

	public void sendRateLimitedMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Sends a message immediately.
		// A new message can't be sent again for a set amount of time.
		if (isOneInstanceOnly && isTimerOn || e == null) {
			return;
		}

		TimerTask sendTask = new TimerTask() {

			@Override
			public void run() {
				e.sendMessage(text);
			}
		};

		startDelayTimer();
		sendTask.run();
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
			// System.out.println("Date adjusted to: " + this.startTime.toString());
		} else {
			// Whether null or not
			this.startTime = startTime;
			// if (startTime != null)
			// System.out.println("Date adjusted to: " + this.startTime.toString());
			// else
			// System.out.println("Date is null");
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
}
