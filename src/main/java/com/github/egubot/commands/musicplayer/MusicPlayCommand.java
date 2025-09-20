package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.SoundPlayback;

public class MusicPlayCommand implements Command {

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		SoundPlayback.play(msg, arguments);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Play music from a URL or search for a track to play";
	}

	@Override
	public String getUsage() {
		return getName() + " <url or search terms>";
	}

	@Override
	public String getCategory() {
		return "Music";
	}
}