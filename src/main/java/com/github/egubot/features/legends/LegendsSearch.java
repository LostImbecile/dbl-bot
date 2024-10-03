package com.github.egubot.features.legends;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.Messageable;
import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.handlers.GenericInteractionHandler;
import com.github.egubot.main.Bot;
import com.github.egubot.objects.legends.CharacterHash;
import com.github.egubot.objects.legends.Characters;

public class LegendsSearch extends LegendsPool {

	public LegendsSearch(List<String> rollTemplates) {
		super(rollTemplates);
	}

	public void search(String msgText, Messageable e) {
		ArrayList<Characters> pool = (ArrayList<Characters>) getPool(msgText);
		if (pool.isEmpty()) {
			e.sendMessage("<:huh:1184466187938185286>");
			return;
		}

		GenericInteractionHandler<Characters> handler = new GenericInteractionHandler<>(Bot.getApi(), pool,
				MessageFormats::createCharacterEmbed,
				(currentPage, totalPages) -> String.format("Page %d of %d", currentPage, totalPages), 10, 15);

		MessageBuilder initialMessage = new MessageBuilder()
				.setContent(String.format("Found %d character%s.", pool.size(), pool.size() > 1 ? "s" : ""));
		handler.sendInitialMessage(initialMessage, e);

	}

	@Override
	protected List<Characters> getPool(String msgText) {
		CharacterHash filterPool;
		CharacterHash namePool;

		String st = msgText.toLowerCase();

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

		name = name.toLowerCase().replace("ssb", "ssgss").replace("dbl", "").replace("-", "")
				.replace("frieza_no_brother", "cooler").replace("best_unit_in_the_game", "5101s").strip();

		ArrayList<Characters> charactersList = (ArrayList<Characters>) LegendsDatabase.getCharactersList();

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
