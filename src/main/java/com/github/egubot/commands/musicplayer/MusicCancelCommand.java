package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.TrackScheduler;

public class MusicCancelCommand implements Command {

	@Override
	public String getName() {
		return "cancel";
	}

	@Override
	public String getDescription() {
		return "Cancel and stop all tracks";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Music";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		TrackScheduler.destroy(ServerInfoUtilities.getServerID(msg));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}