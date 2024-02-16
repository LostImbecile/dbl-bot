package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.SoundPlayback;
import com.github.egubot.interfaces.Command;

public class MusicBufferSizeCommand implements Command {
	private static final int MINUTE = 60 * 60 * 1000;

	@Override
	public String getName() {
		return "buffer";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(arguments.equals("big")) {
			SoundPlayback.getLocalplayermanager().setFrameBufferDuration(3 * MINUTE);
			SoundPlayback.getRemoteplayermanager().setFrameBufferDuration(3 * MINUTE);
		}else {
			SoundPlayback.getLocalplayermanager().setFrameBufferDuration(400);
			SoundPlayback.getRemoteplayermanager().setFrameBufferDuration(400);
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
