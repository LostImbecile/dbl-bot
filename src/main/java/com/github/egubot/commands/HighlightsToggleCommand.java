package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.HighlightsFeature;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class HighlightsToggleCommand implements Command {

	@Override
	public String getName() {
		return "highlight toggle";
	}

	@Override
	public String getDescription() {
		return "Toggle the highlights feature on or off for the server";
	}

	@Override
	public String getUsage() {
		return getName();
	}

	@Override
	public String getCategory() {
		return "Features";
	}

	@Override
	public PermissionLevel getPermissionLevel() {
		return PermissionLevel.ADMIN;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!UserInfoUtilities.canManageServer(msg)) {
			msg.getChannel().sendMessage("❌ You need **Manage Server** permission to configure highlights.");
			return true;
		}

		if (HighlightsFeature.isServerEnabled(msg)) {
			HighlightsFeature.disableServer(msg);
			msg.getChannel().sendMessage("Disabled highlights for this server.");
		} else {
			HighlightsFeature.enableServer(msg);
			msg.getChannel().sendMessage("Enabled highlights for this server.");
		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}