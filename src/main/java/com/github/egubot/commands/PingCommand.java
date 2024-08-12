package com.github.egubot.commands;

import java.time.Instant;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;

public class PingCommand implements Command {

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		Instant timeStamp = msg.getCreationTimestamp();
		Instant now = Instant.now();
		long ms = now.toEpochMilli() - timeStamp.toEpochMilli();

		msg.getChannel().sendMessage("Received in " + ms + "ms");
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
