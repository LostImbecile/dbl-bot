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
	public String getDescription() {
		return "Send a message to Gemini AI and get an AI-generated response";
	}

	@Override
	public String getUsage() {
		return getName() + " <message>";
	}

	@Override
	public String getCategory() {
		return "AI";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		CompletableFuture<List<String>> imageLinks;
		if (msg.getReferencedMessage().isPresent()) {
			imageLinks = MessageInfoUtilities.getReferencedMessageImageLinks(msg)
					.thenApply(optionalLinks -> optionalLinks.orElse(List.of()));
		} else {
			imageLinks = MessageInfoUtilities.getImageLinks(msg);
		}
		
		imageLinks.thenAccept(links -> {
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