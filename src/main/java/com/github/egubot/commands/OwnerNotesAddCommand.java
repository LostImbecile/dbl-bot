package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.OwnerNotes;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class OwnerNotesAddCommand implements Command {

	@Override
	public String getName() {
		return "note";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if(UserInfoUtilities.isOwner(msg)) {
			OwnerNotes.addNote(msg, arguments);
			return true;
		}

		msg.getChannel().sendMessage("Only the bot owner can use this command.");
		return false;
	}

}
