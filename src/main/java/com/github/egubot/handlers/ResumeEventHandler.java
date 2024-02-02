package com.github.egubot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.listener.connection.ResumeListener;

import com.github.egubot.shared.Shared;

public class ResumeEventHandler implements ResumeListener {
	private static final Logger logger = LogManager.getLogger(ResumeEventHandler.class.getName());

	@Override
	public void onResume(ResumeEvent event) {
		Shared.getStatus().changeActivity();
		logger.warn("Bot resumed successfully and is active.");
	}

}
