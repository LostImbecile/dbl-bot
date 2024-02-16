package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.SoundPlayback;
import com.github.egubot.interfaces.Command;

public class MusicPlayCommand implements Command {

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		SoundPlayback.play(msg);
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
