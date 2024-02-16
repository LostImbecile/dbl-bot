package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;

public class BotMessageEditCommand implements Command {

	@Override
	public String getName() {
		return "message edit";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg)) {
			String id = arguments.substring(0, arguments.indexOf(" "));
			String edit = arguments.substring(arguments.indexOf(" "));
			try {
				Bot.getApi().getMessageById(id, msg.getChannel()).get().edit(edit);
			} catch (Exception e) {
				logger.error("Failed to edit a message.", e);
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
