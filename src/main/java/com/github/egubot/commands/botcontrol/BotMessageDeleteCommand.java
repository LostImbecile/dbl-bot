package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;

public class BotMessageDeleteCommand implements Command {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "message delete";
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
		// TODO Auto-generated method stub
		return true;
	}

}
