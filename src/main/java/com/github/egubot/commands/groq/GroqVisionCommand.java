package com.github.egubot.commands.groq;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.message.Message;

import com.github.egubot.facades.AIContext;
import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.groq.api.GroqLLavaAI;

public class GroqVisionCommand implements Command {

	@Override
	public String getName() {
		return "vision";
	}

	@Override
	public boolean execute(Message msg, String arguments) throws Exception {
		CompletableFuture<List<String>> linksFuture;
		if (msg.getReferencedMessage().isPresent()) {
			linksFuture = MessageInfoUtilities.getReferencedMessageLinks(msg, true)
					.thenApply(optionalLinks -> optionalLinks.orElse(List.of()));
		} else {
			linksFuture = MessageInfoUtilities.getMessageLinks(msg, true);
		}

		linksFuture.thenAccept(links -> {
			if (links.isEmpty()) {
				msg.getChannel().sendMessage("No embed is present.");
				return;
			}

			try {
				msg.reply(((GroqLLavaAI) AIContext.getGroqVision()).sendRequest(arguments, links.get(0)).getResponse());
			} catch (IOException e) {
			}

		});

		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
