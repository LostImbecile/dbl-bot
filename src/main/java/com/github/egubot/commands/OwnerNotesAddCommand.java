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
	public String getDescription() {
		return "Add a note to the owner's personal notes";
	}

	@Override
	public String getUsage() {
		return getName() + " <note text>";
	}

	@Override
	public String getCategory() {
		return "Owner";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OWNER;
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

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}