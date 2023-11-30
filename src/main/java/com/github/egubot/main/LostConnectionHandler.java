package com.github.egubot.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.javacord.api.event.connection.LostConnectionEvent;
import org.javacord.api.listener.connection.LostConnectionListener;

public class LostConnectionHandler implements LostConnectionListener{

	@Override
	public void onLostConnection(LostConnectionEvent event) {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd-hh:mm:ss"));
		System.out.println("\n" + date + ": Lost Connection.");
	}

}
