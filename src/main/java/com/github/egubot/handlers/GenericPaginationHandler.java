package com.github.egubot.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GenericPaginationHandler<T> implements MessageComponentCreateListener {

	private final DiscordApi api;
	private final List<T> items;
	private final Function<T, EmbedBuilder> embedCreator;
	private final BiFunction<Integer, Integer, String> footerCreator;
	private final int itemsPerPage;
	private final long timeoutMinutes;
	private int currentPage = 1;
	private Message message;
	private final String uniqueId;
	private final String prevButtonId;
	private final String nextButtonId;

	public GenericPaginationHandler(DiscordApi api, List<T> items, Function<T, EmbedBuilder> embedCreator,
			BiFunction<Integer, Integer, String> footerCreator, int itemsPerPage, long timeoutMinutes) {
		this.api = api;
		this.items = items;
		this.embedCreator = embedCreator;
		this.footerCreator = footerCreator;
		this.itemsPerPage = itemsPerPage;
		this.timeoutMinutes = timeoutMinutes;
		this.uniqueId = UUID.randomUUID().toString();
		this.prevButtonId = "prev_" + uniqueId;
		this.nextButtonId = "next_" + uniqueId;
	}

	public void sendInitialMessage(MessageBuilder originalMessage, Messageable e) {
		originalMessage.append(getContentString()).setEmbeds(createEmbeds()).addComponents(
				ActionRow.of(Button.secondary(prevButtonId, "Previous"), Button.secondary(nextButtonId, "Next")));

		originalMessage.send(e).thenAcceptAsync(t -> {
			this.message = t;
			api.addMessageComponentCreateListener(this).removeAfter(timeoutMinutes, TimeUnit.MINUTES)
					.addRemoveHandler(() -> t.edit("Page navigation timed out."));
		});
	}

	private String getContentString() {
		return String.format(" You have %d minute%s to navigate.", timeoutMinutes, timeoutMinutes > 1 ? "s" : "");
	}

	private EmbedBuilder[] createEmbeds() {
		int startIndex = (currentPage - 1) * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, items.size());
		EmbedBuilder[] embeds = new EmbedBuilder[endIndex - startIndex];

		for (int i = startIndex; i < endIndex; i++) {
			embeds[i - startIndex] = embedCreator.apply(items.get(i));
			if (i == endIndex - 1) {
				String footer = footerCreator.apply(currentPage, getTotalPages());
				embeds[i - startIndex].setFooter(footer);
			}
		}

		return embeds;
	}

	private int getTotalPages() {
		return (items.size() + itemsPerPage - 1) / itemsPerPage;
	}

	@Override
	public void onComponentCreate(MessageComponentCreateEvent event) {
		MessageComponentInteraction interaction = event.getMessageComponentInteraction();
		String customId = interaction.getCustomId();

		if (!interaction.getMessage().equals(message)) {
			return; // Ignore interactions from other messages
		}

		if (customId.equals(prevButtonId)) {
			if (currentPage > 1) {
				currentPage--;
				message.edit(createEmbeds());
			}
		} else if (customId.equals(nextButtonId) && currentPage < getTotalPages()) {
			currentPage++;
			message.edit(createEmbeds());
		}

		interaction.acknowledge();
	}

}