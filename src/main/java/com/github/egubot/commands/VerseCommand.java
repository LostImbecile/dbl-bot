package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.interfaces.DiscordTimerTask;
import com.github.egubot.shared.utils.FileUtilities;

public class VerseCommand implements Command, DiscordTimerTask {

	@Override
	public String getName() {
		return "verse";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		sendVerse(msg.getChannel());
		return true;
	}

	public void sendVerse(Messageable msg) {
		msg.sendMessage(
				FileUtilities.readURL("https://labs.bible.org/api/?passage=random&type=text&formatting=plain"));
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

	@Override
	public boolean execute(long targetChannel, String arguments) throws Exception {
		sendVerse(ServerInfoUtilities.getTextableRegularServerChannel(targetChannel));
		return true;
	}

}
