package com.github.egubot.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.MessageInfoUtilities;
import com.github.egubot.interfaces.Command;

public class DLCommand implements Command {

	@Override
	public String getName() {
		return "dl";
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
				msg.getChannel().sendMessage("No embed is present.");
				return;
			}
			StringBuilder content = new StringBuilder();
			int i = 1;
			for (String link : links) {
				content.append("- [Link " + i++ + "](" + link + ")").append("\n");
			}
			String link = content.toString();
			msg.reply("Links:\n" + link);
		});
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
