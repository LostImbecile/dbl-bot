package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.FileUtilities;

public class VerseCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "verse";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		msg.getChannel().sendMessage(
				FileUtilities.readURL("https://labs.bible.org/api/?passage=random&type=text&formatting=plain"));
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		// TODO Auto-generated method stub
		return true;
	}

}
