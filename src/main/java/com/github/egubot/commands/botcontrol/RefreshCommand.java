package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.StorageFacadesHandler;
import com.github.egubot.handlers.MessageCreateEventHandler;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.shared.utils.DateUtils;

public class RefreshCommand implements Command {

	@Override
	public String getName() {
		return "refresh";
	}

	@Override
	public String getDescription() {
		return "Refresh the bot's internal state and reload configuration settings";
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
			msg.getChannel().sendMessage("Refreshing...").join();

			refresh();

			msg.getChannel().sendMessage("Refreshed :ok_hand:");
		} else {
			msg.getChannel().sendMessage("no");
		}
		return true;
	}

	public static void refresh() {
		printMessages();

		// To make sure data is flushed or uploaded first
		MessageCreateEventHandler.shutdownInternalClasses();

		StorageFacadesHandler.initialise();
	}

	public static void printMessages() {
		String time = DateUtils.getDateTimeNow();
		StreamRedirector.println("", "\n" + time + ": Refreshing Storage.");

		// For GUI
		StreamRedirector.clearStream("info");
		StreamRedirector.printlnOnce("info", "Refreshed at " + time);
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}