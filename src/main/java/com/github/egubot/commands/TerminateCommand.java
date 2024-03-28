package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.main.Bot;
import com.github.egubot.main.Main;
import com.github.egubot.shared.Shared;

public class TerminateCommand implements Command {

	@Override
	public String getName() {
		return "terminate";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (arguments.isBlank() || arguments.equals(Bot.getYourself().getMentionTag())) {
			boolean isOwner = UserInfoUtilities.isOwner(msg);
			
			if ( isOwner || ServerInfoUtilities.getServer(msg).getOwnerId() == msg.getAuthor().getId()) {
				msg.getChannel().sendMessage("Terminating...").join();
				Main.logger.warn("Terminate message invoked.");
				Shared.getShutdown().initiateShutdown(0);
			} else {
				msg.getChannel().sendMessage("<a:no:1195656310356717689>");
			}
		}
		
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
