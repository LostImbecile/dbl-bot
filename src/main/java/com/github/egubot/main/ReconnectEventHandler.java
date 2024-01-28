package com.github.egubot.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.api.listener.connection.ReconnectListener;

public class ReconnectEventHandler implements ReconnectListener{
	private static final Logger logger = LogManager.getLogger(ReconnectEventHandler.class.getName());
	private StatusManager statusManager;
	public ReconnectEventHandler(boolean testMode, DiscordApi api){
		statusManager = new StatusManager(api, testMode);
	}
	@Override
	public void onReconnect(ReconnectEvent event) {
		statusManager.changeActivity();;
		logger.warn("Bot reconnected successfully and is active.");
	}

}
