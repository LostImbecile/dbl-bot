package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.OwnerNotes;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class OwnerNotesRemoveCommand implements Command {

	@Override
	public String getName() {
		return "note remove";
	}

	@Override 
	public boolean execute(Message msg, String arguments) throws Exception {
		if (UserInfoUtilities.isOwner(msg)) {
			int id = 0;
			try {
				id = Integer.parseInt(arguments);
			} catch (Exception e) {
			}
			if (id > 0) {
				OwnerNotes.removeNote(msg, id);
			} else {
				msg.getChannel().sendMessage("Invalid note ID.");
			}

			return true;
		}

		msg.getChannel().sendMessage("Only the bot owner can use this command.");
		return false;
	}

}
