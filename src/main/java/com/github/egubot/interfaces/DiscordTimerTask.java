package com.github.egubot.interfaces;

public interface DiscordTimerTask {
	public String getName();
	
	public boolean execute(long targetChannel, String arguments) throws Exception;
}
