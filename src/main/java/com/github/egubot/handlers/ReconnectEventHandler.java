package com.github.egubot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.api.listener.connection.ReconnectListener;

import com.github.egubot.shared.Shared;

public class ReconnectEventHandler implements ReconnectListener{
	private static final Logger logger = LogManager.getLogger(ReconnectEventHandler.class.getName());
	
	@Override
	public void onReconnect(ReconnectEvent event) {
		Shared.getStatus().changeActivity();;
		logger.warn("Bot reconnected successfully and is active.");
	}

}
