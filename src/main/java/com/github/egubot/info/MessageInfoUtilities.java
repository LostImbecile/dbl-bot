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
		return getReferencedMessageLinks(msg, false, false);
	}
	
	public static CompletableFuture<List<String>> getMessageLinks(Message msg) {
		return getMessageLinks(msg, false, false);
	}
	
	public static CompletableFuture<Optional<List<String>>> getReferencedMessageLinks(Message msg, boolean ignoreVideo) {
		return getReferencedMessageLinks(msg, ignoreVideo, false);
	}
	
	public static CompletableFuture<List<String>> getMessageLinks(Message msg, boolean ignoreVideo) {
		return getMessageLinks(msg, ignoreVideo, false);
	}
	
	public static CompletableFuture<Optional<List<String>>> getReferencedMessageLinks(Message msg, boolean ignoreVideo,
			boolean ignoreImage) {
		return CompletableFuture.supplyAsync(() -> {
			Optional<Message> referencedMessage = msg.getReferencedMessage();
			if (referencedMessage.isPresent()) {
				return Optional.of(getLinksFromMessage(referencedMessage.get(), ignoreVideo, ignoreImage));
			} else {
				return Optional.empty();
			}
		});
	}

	public static CompletableFuture<List<String>> getMessageLinks(Message msg, boolean ignoreVideo,
			boolean ignoreImage) {
		return CompletableFuture.supplyAsync(() -> {
			List<Embed> embeds = msg.getEmbeds();
			List<MessageAttachment> attachments = msg.getAttachments();

			if (!embeds.isEmpty() || !attachments.isEmpty()) {
				return getLinksFromMessage(msg, ignoreVideo, ignoreImage);
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
					embeds = msg.getEmbeds();
					attachments = msg.getAttachments();
					if (!embeds.isEmpty() || !attachments.isEmpty()) {
						return getLinksFromMessage(msg, ignoreVideo, ignoreImage);
					}
				} catch (InterruptedException e) {
				}

				return new ArrayList<>();
			}
		});
	}

	private static List<String> getLinksFromMessage(Message message, boolean ignoreVideo, boolean ignoreImage) {
		List<Embed> embeds = message.getEmbeds();
		List<MessageAttachment> attachments = message.getAttachments();
		List<String> links = new ArrayList<>();
		
		if(ignoreImage && ignoreVideo)
			return links;

		for (Embed embed : embeds) {
			if (!ignoreVideo && embed.getVideo().isPresent()) {
				links.add(embed.getVideo().get().getUrl().toString());
			}
			if (!ignoreImage && embed.getImage().isPresent()) {
				links.add(embed.getImage().get().getUrl().toString());
			}
			if (!ignoreImage && embed.getThumbnail().isPresent()) {
				links.add(embed.getThumbnail().get().getUrl().toString());
			}
		}

		if (!attachments.isEmpty()) {
			links.addAll(attachments.stream().map(MessageAttachment::getUrl).map(Object::toString).toList());
		}

		return links;
	}
}
