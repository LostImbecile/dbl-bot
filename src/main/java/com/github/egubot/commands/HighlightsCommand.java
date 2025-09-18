package com.github.egubot.commands;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import com.github.egubot.features.HighlightsFeature;
import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class HighlightsCommand implements Command {

	@Override
	public String getName() {
		return "highlights";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		if (arguments == null || arguments.trim().isEmpty()) {
			showStatus(msg);
			return true;
		}

		String[] parts = arguments.trim().split("\\s+");
		
		if (parts.length == 1 && parts[0].equalsIgnoreCase("status")) {
			showStatus(msg);
			return true;
		}

		if (!UserInfoUtilities.canManageServer(msg)) {
			msg.getChannel().sendMessage("❌ You need **Manage Server** permission to configure highlights.");
			return true;
		}

		if (parts.length < 3) {
			showUsage(msg);
			return true;
		}

		String emoji = parts[0];
		String channelArg = parts[1];
		int threshold;
		
		try {
			threshold = Integer.parseInt(parts[2]);
			if (threshold < 1 || threshold > 50) {
				msg.getChannel().sendMessage("Threshold must be between 1 and 50 reactions.");
				return true;
			}
		} catch (NumberFormatException e) {
			msg.getChannel().sendMessage("Invalid threshold number. Please provide a valid number between 1 and 50.");
			return true;
		}

		TextChannel targetChannel;
		if (channelArg.equalsIgnoreCase("here")) {
			targetChannel = msg.getChannel();
		} else {
			try {
				long channelID = Long.parseLong(channelArg.replaceAll("[<>#]", ""));
				targetChannel = msg.getServer().orElseThrow().getTextChannelById(channelID).orElse(null);
				
				if (targetChannel == null) {
					msg.getChannel().sendMessage("Invalid channel ID or channel not found.");
					return true;
				}
			} catch (NumberFormatException e) {
				msg.getChannel().sendMessage("Invalid channel format. Use 'here' or mention a channel.");
				return true;
			}
		}

		HighlightsFeature.enableServer(msg);
		HighlightsFeature.setEmoji(msg, emoji);
		HighlightsFeature.setChannel(msg, targetChannel.getId());
		HighlightsFeature.setThreshold(msg, threshold);

		msg.getChannel().sendMessage("✅ Highlights configured successfully!\n" +
				"Emoji: " + emoji + "\n" +
				"Channel: <#" + targetChannel.getId() + ">\n" +
				"Threshold: " + threshold + " reactions");

		return true;
	}

	private void showUsage(Message msg) {
		StringBuilder usage = new StringBuilder("**Highlights Configuration:**\n\n");
		usage.append("**Setup:** `highlights <emoji> <channel> <threshold>`\n");
		usage.append("- `<emoji>` - The emoji to trigger highlights (e.g., 🔥 or <:custom:123>)\n");
		usage.append("- `<channel>` - Use 'here' for current channel or #channel-mention\n");
		usage.append("- `<threshold>` - Number of reactions needed (1-50)\n\n");
		usage.append("**Example:** `highlights 🔥 here 5`\n");
		usage.append("**Status:** `highlights status`\n");
		msg.getChannel().sendMessage(usage.toString());
	}

	private void showStatus(Message msg) {
		StringBuilder status = new StringBuilder("**Highlights Configuration:**\n");
		
		status.append("Enabled: ").append(HighlightsFeature.isServerEnabled(msg) ? "✅ Yes" : "❌ No").append("\n");
		
		String emoji = HighlightsFeature.getEmoji(msg);
		status.append("Emoji: ").append(emoji != null ? emoji : "❌ Not set").append("\n");
		
		int threshold = HighlightsFeature.getThreshold(msg);
		status.append("Threshold: ").append(threshold).append(" reactions\n");
		
		TextChannel channel = HighlightsFeature.getChannel(msg);
		if (channel != null) {
			status.append("Channel: <#").append(channel.getId()).append(">\n");
		} else {
			Long channelID = HighlightsFeature.getChannelID(msg);
			if (channelID != null) {
				status.append("Channel: ❌ Channel not found (ID: ").append(channelID).append(")\n");
			} else {
				status.append("Channel: ❌ Not set\n");
			}
		}
		
		if (HighlightsFeature.isValidConfiguration(msg)) {
			status.append("\n✅ **Ready to use!**");
		} else {
			status.append("\n❌ **Please configure highlights using:** `highlights <emoji> <channel> <threshold>`");
		}
		
		msg.getChannel().sendMessage(status.toString());
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}