package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;

public class BotMessageDeleteCommand implements Command {

	@Override
	public String getName() {
		return "message delete";
	}

	@Override
	public String getDescription() {
		return "Delete a specific bot message by ID";
	}

	@Override
	public String getUsage() {
		return getName() + " <message_id>";
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
			try {
				Bot.getApi().getMessageById(arguments, msg.getChannel()).get().delete();
			} catch (Exception e) {
				logger.error("Failed to delete a message.", e);
				Thread.currentThread().interrupt();
			} 
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}


}