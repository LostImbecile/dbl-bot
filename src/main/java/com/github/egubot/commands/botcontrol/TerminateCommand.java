package com.github.egubot.commands.botcontrol;

import org.javacord.api.entity.message.Message;

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
	public String getDescription() {
		return "Safely terminate the bot application";
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
		if (arguments.isBlank() || arguments.equals(Bot.getYourself().getMentionTag())) {
			boolean isOwner = UserInfoUtilities.isOwner(msg);

			if (isOwner) {
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