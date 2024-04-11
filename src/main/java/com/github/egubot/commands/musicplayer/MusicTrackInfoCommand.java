package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.SoundPlayback;
import com.github.egubot.interfaces.Command;

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

}
