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
		TrackScheduler.skip(ServerInfoUtilities.getServerID(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
