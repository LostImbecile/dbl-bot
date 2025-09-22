package com.github.egubot.commands;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.features.ServerNotificationsFeature;
import com.github.egubot.features.ServerNotificationsFeature.ServerNotificationConfig;
import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ServerNotifyStatusCommand implements Command {

	@Override
	public String getName() {
		return "notify status";
	}

	@Override
	public String getDescription() {
		return "View server notification configuration status";
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
		return PermissionLevel.EVERYONE;
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = ServerNotificationsFeature.getConfig(serverID);
		
		StringBuilder status = new StringBuilder("**Server Notifications Status:**\n\n");
		
		if (config == null || config.channelID == 0) {
			status.append("❌ **Not configured**\n");
			status.append("Use `notify both <channel>` to get started.");
		} else {
			status.append("Join notifications: ").append(config.joinEnabled ? "✅ Enabled" : "❌ Disabled").append("\n");
			status.append("Leave notifications: ").append(config.leaveEnabled ? "✅ Enabled" : "❌ Disabled").append("\n");
			
			TextChannel channel = ServerNotificationsFeature.getChannel(msg.getServer().orElseThrow(), serverID);
			if (channel != null) {
				status.append("Channel: <#").append(channel.getId()).append(">\n");
			} else {
				status.append("Channel: ❌ Channel not found (ID: ").append(config.channelID).append(")\n");
			}
			
			if (ServerNotificationsFeature.hasValidConfiguration(serverID)) {
				status.append("\n✅ **Configuration is valid and active!**");
			} else {
				status.append("\n⚠️ **Please check your configuration**");
			}
		}
		
		msg.getChannel().sendMessage(status.toString());
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}