package com.github.egubot.main;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.api.listener.connection.ResumeListener;

public class ResumeEventHandler extends StatusManager implements ResumeListener{
	
	public ResumeEventHandler(boolean testMode, DiscordApi api){
		super(api,testMode);
	}
	
	@Override
	public void onResume(ResumeEvent event) {
		changeActivity();
		System.out.println("\nBot resumed successfully and is active.");
	}

}
