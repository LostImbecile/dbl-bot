package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.StorageFacadesHandler;
import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class RefreshCommand implements Command {

	@Override
	public String getName() {
		return "refresh";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isOwner(msg)) {
			msg.getChannel().sendMessage("Refreshing...").join();
			System.out.println("\nRefreshing " + MessageCreateEventHandler.class.getName() + ".");

			// Important to make sure any remaining data is uploaded first
			MessageCreateEventHandler.shutdownInternalClasses();

			StorageFacadesHandler.initialise();

			msg.getChannel().sendMessage("Refreshed :ok_hand:");
		} else {
			msg.getChannel().sendMessage("no");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}
