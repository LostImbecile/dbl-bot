package com.github.egubot.commands;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.features.ServerNotificationsFeature;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ServerJoinNotifyCommand implements Command {

	@Override
	public String getName() {
		return "join notify";
	}

	@Override
	public String getDescription() {
		return "Setup server join notifications in a channel";
	}

	@Override
	public String getUsage() {
		return getName() + " <channel>";
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

		if (arguments == null || arguments.trim().isEmpty()) {
			msg.getChannel().sendMessage("**Usage:** `" + getUsage() + "`\n" +
					"**Example:** `join notify here` or `join notify #general`");
			return true;
		}

		String channelArg = arguments.trim();
		TextChannel targetChannel;

		if (channelArg.equalsIgnoreCase("here")) {
			targetChannel = msg.getChannel();
		} else {
			try {
				long channelID = Long.parseLong(channelArg.replaceAll("[<>#]", ""));
				targetChannel = msg.getServer().orElseThrow().getTextChannelById(channelID).orElse(null);
				
				if (targetChannel == null) {
					msg.getChannel().sendMessage("❌ Invalid channel ID or channel not found.");
					return true;
				}
			} catch (NumberFormatException e) {
				msg.getChannel().sendMessage("❌ Invalid channel format. Use 'here' or mention a channel.");
				return true;
			}
		}

		ServerNotificationsFeature.setChannel(msg, targetChannel.getId());
		ServerNotificationsFeature.enableJoinNotifications(msg);
		msg.getChannel().sendMessage("✅ Join notifications enabled for <#" + targetChannel.getId() + ">");

		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}