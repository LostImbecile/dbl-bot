package com.github.egubot.features;

import java.time.Instant;
import java.util.Date;
import java.util.TimerTask;

import org.javacord.api.entity.message.Messageable;

import com.github.egubot.shared.TimedAction;

public class MessageTimers extends TimedAction{

	public MessageTimers(long length, Date startTime, Instant otherTimeFormat) {
		super(length, startTime, otherTimeFormat);
	}

	public void sendDelayedMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Sends a message after a set amount of time
		if (isOneInstanceOnly && isTimerOn() || e == null) {
			return;
		}

		TimerTask sendTask = new TimerTask() {

			@Override
			public void run() {
				e.sendMessage(text);
				setTimerOn(false);
			}
		};

		startSingleTimer(sendTask);
	}

	public void sendScheduledMessage(Messageable e, String text, boolean isOneInstanceOnly) {
		// Schedules a message to be sent every set amount of time
		// Date is adjusted once only in this.
		if (isOneInstanceOnly && isRecurringTimerOn() || e == null) {
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
		if (isOneInstanceOnly && isTimerOn() || e == null) {
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
		if (isOneInstanceOnly && isTimerOn() || e == null) {
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
}
