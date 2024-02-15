package com.github.egubot.interfaces;

import org.javacord.api.entity.message.Message;

public interface Command {

	public String getName();
	
	public boolean execute(Message msg, String arguments);
}
