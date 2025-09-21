package com.github.egubot.commands.gemini;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.interfaces.Command;
import com.google.gemini.GeminiAI;

public class VisionCommand implements Command {

	@Override
	public String getName() {
		return "vision";
	}

	@Override
	public String getDescription() {
		return "Analyze images using Gemini AI vision capabilities with optional text prompt";
	}

	@Override
	public String getUsage() {
		return getName() + " [prompt] (with image attachment or reply to image)";
	}

	@Override
	public String getCategory() {
		return "AI";
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
			if (links.isEmpty()) {
				msg.getChannel().sendMessage("No image is present.");
				return;
			}

			try {
				GeminiAI gemini = new GeminiAI();
				String prompt = arguments.isEmpty() ? "What's in this image?" : arguments;
				String response = gemini.sendRequestWithImage(prompt, links.get(0)).getResponse();
				msg.reply(response);
			} catch (IOException e) {
				msg.getChannel().sendMessage("Error processing image: " + e.getMessage());
			} catch (Exception e) {
				msg.getChannel().sendMessage("Vision command error: " + e.getMessage());
			}
		});

		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}
}