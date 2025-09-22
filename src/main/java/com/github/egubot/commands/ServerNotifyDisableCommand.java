package com.github.egubot.commands;

import org.javacord.api.entity.message.Message;

import com.github.egubot.features.ServerNotificationsFeature;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ServerNotifyDisableCommand implements Command {

	@Override
	public String getName() {
		return "notify disable";
	}

	@Override
	public String getDescription() {
		return "Disable all server join/leave notifications";
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
		return PermissionLevel.MOD;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (!UserInfoUtilities.canManageServer(msg)) {
			msg.getChannel().sendMessage("❌ You need **Manage Server** permission to configure server notifications.");
			return true;
		}

		ServerNotificationsFeature.disableJoinNotifications(msg);
		ServerNotificationsFeature.disableLeaveNotifications(msg);
		msg.getChannel().sendMessage("✅ Server notifications disabled.");

		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}