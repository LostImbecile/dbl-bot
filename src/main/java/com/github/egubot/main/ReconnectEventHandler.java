package com.github.egubot.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.api.listener.connection.ReconnectListener;

public class ReconnectEventHandler extends StatusManager implements ReconnectListener{
	
	public ReconnectEventHandler(boolean testMode, DiscordApi api){
		super(api,testMode);
	}
	@Override
	public void onReconnect(ReconnectEvent event) {
		changeActivity();
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd-hh:mm:ss"));
		System.out.println("\n" + date + ": Bot reconnected successfully and is active.");
	}

}
