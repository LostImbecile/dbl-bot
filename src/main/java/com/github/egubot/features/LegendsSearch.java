package com.github.egubot.features;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.objects.CharacterHash;
import com.github.egubot.objects.Characters;

public class LegendsSearch extends LegendsPool {

	public LegendsSearch(LegendsDatabase legendsWebsite, List<String> rollTemplates) {
		super(legendsWebsite, rollTemplates);
	}

	public void search(String msgText, DiscordApi api, Messageable e) {
		EmbedBuilder[] embeds = new EmbedBuilder[10];
		ArrayList<Characters> pool = (ArrayList<Characters>) getPool(msgText);
		if (pool == null || pool.isEmpty()) {
			e.sendMessage("<:gokuhuh:1009185335881768970>");
			return;
		}

		// Creates 10 or less embeds to start off the message
		for (int i = 0; i < pool.size() && i < 10; i++) {
			embeds[i] = MessageFormats.createCharacterEmbed(pool.get(i));
		}

		MessageBuilder msg = new MessageBuilder();

		msg.setContent("Found " + pool.size() + " character");

		if (pool.size() > 1)
			msg.append("s");

		msg.append(".");

		int timeLimit = 15;
		// Just to make sure buttons don't interfere with each other
		String prev = Instant.now().toString() + "prev" + pool.get(0).getCharacterName();
		String next = Instant.now().toString() + "next" + pool.get(0).getCharacterName();
		if (pool.size() > 10) {
			// Adds two buttons to the message to navigate through pages using
			msg.addComponents(ActionRow.of(Button.secondary(prev, "Previous"), Button.secondary(next, "Next")));
			msg.append(" Note, you only have <" + timeLimit + " minutes to go through the pages.");

			String gameId = pool.get(9).getGameID();

			// Add page number to the last embed
			String footer = String.format("%s%n%nPage 0 of %d", gameId, pool.size() / 10);

			embeds[9].setFooter(footer);

			msg.setEmbeds(embeds);

			// Listener to deal with page navigation
			MessageComponentCreateListener navigatePageHandler = new NavigatePageHandler(pool, msg.send(e).join(), prev, next);
			api.addMessageComponentCreateListener(navigatePageHandler).removeAfter(timeLimit, TimeUnit.MINUTES);
		} else {
			msg.setEmbeds(embeds);
			msg.send(e);
		}

	}

	private class NavigatePageHandler implements MessageComponentCreateListener {

		int pageIndex = 0;
		ArrayList<Characters> pool;
		Message msg;
		String previous;
		String next;

		public NavigatePageHandler(List<Characters> pool, Message msg, String previous, String next) {
			this.pool = (ArrayList<Characters>) pool;
			this.msg = msg;
			this.previous = previous;
			this.next = next;
		}

		@Override
		public void onComponentCreate(MessageComponentCreateEvent event) {
			try {
				EmbedBuilder[] embeds = new EmbedBuilder[10];
				MessageComponentInteraction messageComponentInteraction = event.getMessageComponentInteraction();
				String customId = messageComponentInteraction.getCustomId();

				int lastPage = pool.size() / 10;

				if (customId.equals(next)) {
					pageIndex++;

					if (pageIndex > lastPage) {
						pageIndex = lastPage;
					}
				} else if (customId.equals(previous)) {
					pageIndex--;
					if (pageIndex < 0) {
						pageIndex = 0;
					}
				} else {
					return;
				}

				// Creates 10 or less embeds based on the page number, and saves
				// the index of the last embed to change the footer of.
				int lastEmbedIndex = 0;
				for (int i = pageIndex * 10; i < pool.size() && i < (pageIndex + 1) * 10; i++) {
					embeds[i % 10] = MessageFormats.createCharacterEmbed(pool.get(i));
					lastEmbedIndex = i;
				}

				String gameId = pool.get(lastEmbedIndex).getGameID();

				// Add page number to the last embed
				String footer = String.format("%s%n%nPage %d of %d", gameId, pageIndex, lastPage);

				embeds[lastEmbedIndex % 10].setFooter(footer);

				msg.edit(embeds).join();
				messageComponentInteraction.acknowledge();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	protected List<Characters> getPool(String msgText) {
		String st = msgText.replace("b-search", "").trim();
		CharacterHash filterPool;
		CharacterHash namePool;

		int separateIndex = st.indexOf(" ");

		String name;
		try {
			name = st.substring(0, separateIndex);
		} catch (Exception e) {
			name = st;
		}

		namePool = (CharacterHash) getNamePool(name);

		try {
			// If it's positive then there's a space
			// which means a filter is present
			if (separateIndex > 0) {
				// Creates the pool based on the (valid) filters
				filterPool = (CharacterHash) analyseAndCreatePool(st.substring(separateIndex + 1));

				if (namePool.isEmpty()) {

					return filterPool.toArrayList();

				} else {
					// Filters out any characters that don't have
					// all the specified tags
					combineSubPools(filterPool, namePool, "&");

					return filterPool.toArrayList();
				}
			}
		} catch (Exception e) {

		}

		return namePool.toArrayList();
	}

	private Set<Characters> getNamePool(String name) {
		CharacterHash namePool = new CharacterHash();
		if (name.equals("-1")) {
			return namePool;
		}

		name = name.toLowerCase().replace("ssb", "ssgss").replace("dbl", "").replace("-", "").strip();

		ArrayList<Characters> charactersList = (ArrayList<Characters>) getLegendsWebsite().getCharactersList();

		// Split the input name into individual tokens (words)
		String[] nameTokens = name.split("_");
		String characterName;
		String characterID;

		for (Characters character : charactersList) {
			characterName = character.getCharacterName().replace(" ", "_").toLowerCase().strip();
			characterID = character.getGameID().toLowerCase().replace("dbl", "").replace("-", "");

			if (characterID.equals(name)) {
				namePool.put(character);
				break;
			}

			/*
			 * Check if all tokens in the input name are present in the character name
			 * This means the name needs to include all the words or tokens, making
			 * order not matter, and the names not need to be a perfect match.
			 */
			boolean allTokensMatch = true;
			for (String token : nameTokens) {
				if (!characterName.contains(token)) {
					allTokensMatch = false;
					break;
				}
			}

			if (allTokensMatch) {
				namePool.put(character);
			}
		}

		return namePool;
	}
}
