package com.github.egubot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.connection.LostConnectionEvent;
import org.javacord.api.listener.connection.LostConnectionListener;

public class LostConnectionHandler implements LostConnectionListener{
	private static final Logger logger = LogManager.getLogger(LostConnectionHandler.class.getName());
	
	@Override
	public void onLostConnection(LostConnectionEvent event) {
		logger.warn("Lost Connection.");
	}
}
