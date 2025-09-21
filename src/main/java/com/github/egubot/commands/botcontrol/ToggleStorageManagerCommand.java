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
	public String getDescription() {
		return "Toggle storage manager type (local/sqlite)";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Bot Control";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.OWNER;
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg)) {
			DataManagerHandler.toggleSQLite();
			if (!DataManagerHandler.switchAllManagers()) {
				DataManagerHandler.toggleSQLite();
				msg.getChannel().sendMessage("Failed.");
			} else
				msg.getChannel().sendMessage("Success.");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}