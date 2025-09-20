package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.TrackScheduler;

public class MusicResumeCommand implements Command{

	@Override
	public String getName() {
		return "resume";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		TrackScheduler.resume(ServerInfoUtilities.getServerID(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Resume the paused music track";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Music";
	}
}