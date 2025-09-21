package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.OwnerNotes;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.shared.utils.ConvertObjects;

public class OwnerNotesSendCommand implements Command {

	@Override
	public String getName() {
		return "note send";
	}

	@Override
	public String getDescription() {
		return "Display all owner's personal notes";
	}

	@Override
	public String getUsage() {
		return getName();
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
			msg.getChannel().sendMessage(ConvertObjects.stringToInputStream(OwnerNotes.formatNotes()), "notes.md");
			return true;
		}

		msg.getChannel().sendMessage("Only the bot owner can use this command.");
		return false;
	}

}