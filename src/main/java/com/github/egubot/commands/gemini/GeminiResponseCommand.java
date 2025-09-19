package com.github.egubot.commands.gemini;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.interfaces.Command;

public class GeminiResponseCommand implements Command {

	@Override
	public String getName() {
		return "gem";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		CompletableFuture<List<String>> linksFuture;
		if (msg.getReferencedMessage().isPresent()) {
			linksFuture = MessageInfoUtilities.getReferencedMessageLinks(msg)
					.thenApply(optionalLinks -> optionalLinks.orElse(List.of()));
		} else {
			linksFuture = MessageInfoUtilities.getMessageLinks(msg);
		}
		
		linksFuture.thenAccept(links -> {
			AIContext.getGemini().respond(msg, arguments, links.isEmpty() ? null : links);
		}).exceptionally(ex -> {
			AIContext.getGemini().respond(msg, arguments, null);
			return null;
		});
		
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return false;
	}

}