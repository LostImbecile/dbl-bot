package com.github.egubot.interfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

public interface Command {
	public static final Logger logger = LogManager.getLogger(Command.class.getName());
	
	public String getName();
	
	public boolean execute(Message msg, String arguments) throws Exception;
	
	public boolean isStartsWithPrefix();
}
