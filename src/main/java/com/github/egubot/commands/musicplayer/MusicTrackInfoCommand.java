package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.SoundPlayback;

public class MusicTrackInfoCommand implements Command {

	@Override
	public String getName() {
		return "now";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		SoundPlayback.getCurrentTrackInfo(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Display detailed information about the currently playing track";
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