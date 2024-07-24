package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.TrackScheduler;

public class MusicSkipCommand implements Command {

	@Override
	public String getName() {
		return "skip";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		int i;
		try {
			i = Integer.parseInt(arguments);
			i = i < 1 ? 1 : i;
		} catch (NumberFormatException e) {
			i = 1;
		}
		TrackScheduler.skip(ServerInfoUtilities.getServerID(msg), i);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
