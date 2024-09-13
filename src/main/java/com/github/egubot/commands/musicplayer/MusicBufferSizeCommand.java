package com.github.egubot.commands.musicplayer;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.lavaplayer.SoundPlayback;

public class MusicBufferSizeCommand implements Command {
	private static final int MINUTE = 60 * 60 * 1000;

	@Override
	public String getName() {
		return "buffer";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (arguments.equals("big")) {
			SoundPlayback.updateBufferDuration(3 * MINUTE);
		} else if (arguments.equals("small") || arguments.isBlank()) {
			SoundPlayback.updateBufferDuration(400);
		} else {
			try {
				SoundPlayback.updateBufferDuration(Integer.parseInt(arguments));
			} catch (NumberFormatException e) {
			}
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
