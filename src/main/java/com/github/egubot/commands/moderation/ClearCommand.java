package com.github.egubot.commands.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

import com.github.egubot.info.UserInfoUtilities;
import com.github.egubot.interfaces.Command;

public class ClearCommand implements Command {
	public static final String TICK = "✅";
	public static final String CROSS = "❌";

	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public boolean execute(Message msg, String arguments) {
		if (UserInfoUtilities.isAdmin(msg)) {
			if (arguments.isEmpty()) {
				msg.getChannel().sendMessage("You have to define a limit");
				return true;
			}
			int amount = 0;
			try {
				amount = Integer.parseInt(arguments);
			} catch (NumberFormatException e) {
				msg.getChannel().sendMessage("Invalid number");
				return true;
			}
			if (amount < 1) {
				msg.getChannel().sendMessage("Invalid number");
				return true;
			} else if (amount > 100) {
				msg.getChannel().sendMessage("I can't clear more than 100 messages at once");
				return true;
			}

			try {
				MessageSet messages = msg.getChannel().getMessagesBefore(amount, msg).get();
				if (messages.isEmpty()) {
					msg.getChannel().sendMessage("No messages found");
					return true;
				}
				int oldMessageCount = 0;
				Instant oldMessageTime = Instant.now().minus(Duration.ofDays(14));
				for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
					Message message = iterator.next();
					if (message.getCreationTimestamp().isBefore(oldMessageTime)) {
						oldMessageCount++;
					}
				}

				if (oldMessageCount > 10) {
					amount = amount - oldMessageCount + 10;
					msg.getChannel()
							.sendMessage("Some messages older than 14 days were ignored. Only " + amount
									+ " messages to be deleted.")
							.thenAcceptAsync(t -> t.deleteAfter(5, TimeUnit.SECONDS));
				}
				msg.getChannel().deleteMessages(msg.getChannel().getMessagesBefore(amount, msg).get());
				msg.delete();
			} catch (Exception e) {
				msg.getChannel().sendMessage("Failed to delete messages");
			}

		}
		return true;
	}

	@Override
	public boolean isStartsWithPrefix() {
		return true;
	}

}
