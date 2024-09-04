package com.github.egubot.info;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.embed.Embed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MessageInfoUtilities {
	private MessageInfoUtilities() {
	}

	public static CompletableFuture<Optional<List<String>>> getReferencedMessageLinks(Message msg) {
		return CompletableFuture.supplyAsync(() -> {
			Optional<Message> referencedMessage = msg.getReferencedMessage();
			if (referencedMessage.isPresent()) {
				return Optional.of(getLinksFromMessage(referencedMessage.get()));
			} else {
				return Optional.empty();
			}
		});
	}

	 public static CompletableFuture<List<String>> getMessageLinks(Message msg) {
	        return CompletableFuture.supplyAsync(() -> {
	            List<Embed> embeds = msg.getEmbeds();
	            List<MessageAttachment> attachments = msg.getAttachments();

	            if (!embeds.isEmpty() || !attachments.isEmpty()) {
	                return getLinksFromMessage(msg);
	            } else {
	                try {
	                    TimeUnit.MILLISECONDS.sleep(1000); 
	                    embeds = msg.getEmbeds();
	                    attachments = msg.getAttachments();
	                    if (!embeds.isEmpty() || !attachments.isEmpty()) {
	                        return getLinksFromMessage(msg);
	                    }
	                } catch (InterruptedException e) {
	                }

	                return new ArrayList<>();
	            }
	        });
	    }

	private static List<String> getLinksFromMessage(Message message) {
		List<Embed> embeds = message.getEmbeds();
		List<MessageAttachment> attachments = message.getAttachments();

		if (!embeds.isEmpty()) {
			return embeds.stream().map(embed -> {
				if (embed.getVideo().isPresent()) {
					return embed.getVideo().get().getUrl().toString();
				} else if (embed.getImage().isPresent()) {
					return embed.getImage().get().getUrl().toString();
				} else if (embed.getThumbnail().isPresent()) {
					return embed.getThumbnail().get().getUrl().toString();
				} else
					return "";
			}).toList();
		} else if (!attachments.isEmpty()) {
			return attachments.stream().map(MessageAttachment::getUrl).map(Object::toString).toList();
		} else {
			return List.of();
		}
	}
}
