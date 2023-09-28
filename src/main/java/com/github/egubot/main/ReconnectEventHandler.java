package com.github.egubot.main;

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
		System.out.println("\nBot reconnected successfully and is active.");
	}

}
