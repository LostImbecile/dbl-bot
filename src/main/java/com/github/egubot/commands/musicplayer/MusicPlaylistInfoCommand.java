package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.SoundPlayback;

public class MusicPlaylistInfoCommand implements Command {

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		SoundPlayback.getPlaylistInfo(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
