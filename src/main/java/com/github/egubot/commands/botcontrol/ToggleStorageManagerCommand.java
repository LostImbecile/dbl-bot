package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.storage.DataManagerHandler;

public class ToggleStorageManagerCommand implements Command {

	@Override
	public String getName() {
		return "toggle manager";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if(UserInfoUtilities.isOwner(msg)) {
			DataManagerHandler.toggleSQLite();
			DataManagerHandler.switchAllManagers();
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
