package com.github.egubot.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.listener.connection.ResumeListener;

public class ResumeEventHandler extends StatusManager implements ResumeListener {

	public ResumeEventHandler(boolean testMode, DiscordApi api) {
		super(api, testMode);
	}

	@Override
	public void onResume(ResumeEvent event) {
		changeActivity();
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd-hh:mm:ss"));
		System.out.println("\n" + date + ": Bot resumed successfully and is active.");
	}

}
