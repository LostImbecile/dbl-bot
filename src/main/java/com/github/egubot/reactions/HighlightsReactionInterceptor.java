package com.github.egubot.reactions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import com.github.egubot.features.HighlightsFeature;
import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.interfaces.ReactionInterceptor;

public class HighlightsReactionInterceptor implements ReactionInterceptor {

	private static final Map<Long, Integer> messageReactionCounts = new ConcurrentHashMap<>();

	@Override
	public String getName() {
		return "highlights";
	}

	@Override
	public boolean handleReactionAdd(ReactionAddEvent event, Message message, User user, String emoji) throws Exception {
		if (!HighlightsFeature.isValidConfiguration(message)) {
			return false;
		}

		String targetEmoji = HighlightsFeature.getEmoji(message);
		String actualEmoji = getEmojiString(event);
		
		if (!actualEmoji.equals(targetEmoji)) {
			return false;
		}

		long messageId = message.getId();
		int currentCount = messageReactionCounts.getOrDefault(messageId, 0) + 1;
		messageReactionCounts.put(messageId, currentCount);

		int threshold = HighlightsFeature.getThreshold(message);
		if (currentCount >= threshold) {
			sendHighlight(message);
			messageReactionCounts.remove(messageId);
		}

		return false;
	}

	@Override
	public boolean handleReactionRemove(ReactionRemoveEvent event, Message message, User user, String emoji) throws Exception {
		if (!HighlightsFeature.isValidConfiguration(message)) {
			return false;
		}

		String targetEmoji = HighlightsFeature.getEmoji(message);
		String actualEmoji = getEmojiString(event);
		
		if (!actualEmoji.equals(targetEmoji)) {
			return false;
		}

		long messageId = message.getId();
		int currentCount = messageReactionCounts.getOrDefault(messageId, 0) - 1;
		if (currentCount <= 0) {
			messageReactionCounts.remove(messageId);
		} else {
			messageReactionCounts.put(messageId, currentCount);
		}

		return false;
	}

	private String getEmojiString(ReactionAddEvent event) {
		return event.getEmoji().asUnicodeEmoji().orElseGet(() ->
			event.getEmoji().asCustomEmoji().map(customEmoji -> "<:" + customEmoji.getName() + ":" + customEmoji.getId() + ">").orElse("")
		);
	}

	private String getEmojiString(ReactionRemoveEvent event) {
		return event.getEmoji().asUnicodeEmoji().orElseGet(() ->
			event.getEmoji().asCustomEmoji().map(customEmoji -> "<:" + customEmoji.getName() + ":" + customEmoji.getId() + ">").orElse("")
		);
	}

	private void sendHighlight(Message message) {
		TextChannel highlightChannel = HighlightsFeature.getChannel(message);
		if (highlightChannel == null) {
			Long channelID = HighlightsFeature.getChannelID(message);
			if (channelID != null) {
				message.getChannel().sendMessage("❌ Highlights channel (ID: " + channelID + ") was deleted or is no longer accessible.");
			}
			return;
		}

		MessageInfoUtilities.getMessageLinks(message).thenAccept(attachmentLinks -> {
			String content = message.getContent();
			if (content.length() > 1000) {
				content = content.substring(0, 997) + "...";
			}

			boolean hasTextContent = !content.isEmpty();
			boolean hasAttachments = !attachmentLinks.isEmpty();

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("🌟 Highlighted Message")
					.addField("Author", message.getAuthor().getDisplayName(), true)
					.addField("Channel", "<#" + message.getChannel().getId() + ">", true)
					.addField("Jump to Message", "[Click here](" + message.getLink() + ")", true)
					.setColor(java.awt.Color.ORANGE)
					.setTimestamp(message.getCreationTimestamp());

			if (hasTextContent) {
				embed.setDescription(content);
			} else if (hasAttachments) {
				embed.setDescription("*Message contains media attachments*");
			} else {
				embed.setDescription("*No text content*");
			}

			if (message.getAuthor().getAvatar() != null) {
				embed.setThumbnail(message.getAuthor().getAvatar());
			}

			if (hasAttachments) {
				embed.addField("Media", attachmentLinks.size() + " attachment(s)", true);
				
				String firstAttachment = attachmentLinks.get(0);
				if (firstAttachment.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp).*")) {
					embed.setImage(firstAttachment);
				}
				
				if (attachmentLinks.size() > 1) {
					StringBuilder allLinks = new StringBuilder();
					for (int i = 0; i < Math.min(attachmentLinks.size(), 5); i++) {
						allLinks.append("[Media ").append(i + 1).append("](").append(attachmentLinks.get(i)).append(")\n");
					}
					if (attachmentLinks.size() > 5) {
						allLinks.append("... and ").append(attachmentLinks.size() - 5).append(" more");
					}
					embed.addField("All Media Links", allLinks.toString(), false);
				}
			}

			highlightChannel.sendMessage(embed);
		});
	}

	@Override
	public boolean canHandle(String emoji, Message message) {
		if (!HighlightsFeature.isValidConfiguration(message)) {
			return false;
		}
		return true;
	}

	@Override
	public int getPriority() {
		return 100;
	}
}