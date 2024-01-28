package com.github.egubot.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.listener.connection.ResumeListener;

public class ResumeEventHandler implements ResumeListener {
	private static final Logger logger = LogManager.getLogger(ResumeEventHandler.class.getName());
	private StatusManager statusManager;
	public ResumeEventHandler(boolean testMode, DiscordApi api) {
		statusManager = new StatusManager(api, testMode);
	}

	@Override
	public void onResume(ResumeEvent event) {
		statusManager.changeActivity();
		logger.warn("Bot resumed successfully and is active.");
	}

}
