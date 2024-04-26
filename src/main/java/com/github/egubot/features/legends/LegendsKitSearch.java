package com.github.egubot.features.legends;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.main.Bot;
import com.github.egubot.objects.legends.Characters;

/*
 * Implementation specific, the start() method works
 * for all websites, everything else however is for
 * this specific one.
 */
public class LegendsKitSearch {
	public static final String WEBSITE_URL = "https://dblegends.net/characters";
	private String[] keywords;

	public LegendsKitSearch(String args, Messageable e) throws IOException {
		keywords = args.split(",");
		for (int i = 0; i < keywords.length; i++) {
			keywords[i] = keywords[i].toLowerCase().strip();
		}

		Document document = Jsoup.connect(WEBSITE_URL).get();
		getData(document, e);
	}

	public void getData(Document document, Messageable e) {
		getCharacters(document, e);
	}

	private void getCharacters(Document document, Messageable e) {
		Elements characters = document.select("a.chara-list");
		ArrayList<Characters> pool = new ArrayList<>();
		ArrayList<String> results = new ArrayList<>();
		EmbedBuilder[] embeds = new EmbedBuilder[10];

		for (Element character : characters) {
			String charaUrl = character.attr("href");

			String result = searchPage(charaUrl);
			if (result != null) {
				pool.add(getSiteID(charaUrl));
				results.add(result);
			}
		}

		if (pool == null || pool.isEmpty()) {
			e.sendMessage("Nothing, wasted my time smh <:joea:1144008494568194099>");
			return;
		}

		for (int i = 0; i < pool.size() && i < 10; i++) {
			embeds[i] = MessageFormats.createCharacterEmbed(pool.get(i))
					.setDescription(results.get(i) + MessageFormats.EQUALISE);
		}

		MessageBuilder msg = new MessageBuilder();

		String textContent = "Found " + pool.size() + " character";
		if (pool.size() > 1)
			textContent = textContent + "s";

		textContent = textContent + ".";

		msg.setContent(textContent);

		int timeLimit = 15;
		// Just to make sure buttons don't interfere with each other
		String prev = Instant.now().toString() + "prev" + pool.get(0).getCharacterName();
		String next = Instant.now().toString() + "next" + pool.get(0).getCharacterName();
		if (pool.size() > 10) {
			// Adds two buttons to the message to navigate through pages using
			msg.addComponents(ActionRow.of(Button.secondary(prev, "Previous"), Button.secondary(next, "Next")));
			msg.append(" Note, you only have " + timeLimit + " minutes to go through the pages.");

			String gameId = pool.get(9).getGameID();

			// Add page number to the last embed
			String footer = String.format("%s%n%nPage 1 of %d", gameId, (pool.size() + 9) / 10);

			embeds[9].setFooter(footer);

			msg.setEmbeds(embeds);

			// Listener to deal with page navigation
			MessageComponentCreateListener navigatePageHandler = new NavigatePageHandler(pool, results,
					msg.send(e).join(), prev, next);

			Bot.getApi().addMessageComponentCreateListener(navigatePageHandler).removeAfter(timeLimit, TimeUnit.MINUTES)
					.addRemoveHandler(() -> msg.removeAllComponents()
							.setContent("Found " + pool.size() + " characters. Page navigation timed out."));

		} else {
			msg.setEmbeds(embeds);
			msg.send(e);
		}
	}

	public static Characters getSiteID(String line) {
		try {
			String st = line.replace("/character/", "");
			return LegendsDatabase.getCharacterHash().get(Integer.parseInt(st));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String searchPage(String charaUrl) {
		try {
			String url = "https://dblegends.net" + charaUrl;
			Document doc = Jsoup.connect(url).get();
			Elements elements = doc.select("p, .ps-2");
			String result = null;

			for (Element element : elements) {
				String text = element.text().toLowerCase();
				for (String key : keywords) {
					if (text.contains(key)) {
						result = text.replace(key, "`" + key + "`");
						break;
					}
				}
			}

			return result;
		} catch (IOException e) {
		}
		return null;
	}

	private class NavigatePageHandler implements MessageComponentCreateListener {

		int pageIndex = 1;
		ArrayList<Characters> pool;
		ArrayList<String> results;
		Message msg;
		String previous;
		String next;

		public NavigatePageHandler(List<Characters> pool, List<String> results, Message msg, String previous,
				String next) {
			this.pool = (ArrayList<Characters>) pool;
			this.results = (ArrayList<String>) results;
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

				int lastPage = (pool.size() + 9) / 10;

				if (customId.equals(next)) {
					pageIndex++;

					if (pageIndex > lastPage) {
						pageIndex = lastPage;
					}
				} else if (customId.equals(previous)) {
					pageIndex--;
					if (pageIndex < 1) {
						pageIndex = 1;
					}
				} else {
					return;
				}

				// Creates 10 or less embeds based on the page number, and saves
				// the index of the last embed to change the footer of.
				int lastEmbedIndex = 0;
				for (int i = (pageIndex - 1) * 10; i < pool.size() && i < pageIndex * 10; i++) {
					embeds[i % 10] = MessageFormats.createCharacterEmbed(pool.get(i))
							.setDescription(results.get(i) + MessageFormats.EQUALISE);
					lastEmbedIndex = i;
				}

				String gameId = pool.get(lastEmbedIndex).getGameID();

				// Add page number to the last embed
				String footer = String.format("%s%n%nPage %d of %d", gameId, pageIndex, lastPage);

				embeds[lastEmbedIndex % 10].setFooter(footer);

				msg.edit(embeds).join();
				messageComponentInteraction.acknowledge();
			} catch (Exception e) {
			}

		}

	}

}
