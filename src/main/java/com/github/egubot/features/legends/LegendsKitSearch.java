package com.github.egubot.features.legends;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.egubot.build.LegendsDatabase;
import com.github.egubot.features.MessageFormats;
import com.github.egubot.handlers.GenericInteractionHandler;
import com.github.egubot.main.Bot;
import com.github.egubot.objects.legends.Characters;

public class LegendsKitSearch {
	public static final String WEBSITE_URL = "https://dblegends.net/characters";
	private String[] keywords;
	private static final int CONCURRENT_REQUESTS = 20;

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
		List<CompletableFuture<SearchResult>> futures = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

		for (Element character : characters) {
			String charaUrl = character.attr("href");
			CompletableFuture<SearchResult> future = CompletableFuture.supplyAsync(() -> {
				String result = searchPage(charaUrl);
				Characters chara = result != null ? getSiteID(charaUrl) : null;
				return new SearchResult(chara, result);
			}, executor);
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		List<SearchResult> searchResults = futures.stream().map(CompletableFuture::join)
				.filter(result -> result.character != null).collect(Collectors.toList());

		executor.shutdown();

		if (searchResults.isEmpty()) {
			e.sendMessage("Nothing, wasted my time smh <:joea:1144008494568194099>");
			return;
		}

		GenericInteractionHandler<SearchResult> handler = new GenericInteractionHandler<>(Bot.getApi(), searchResults,
				this::createCustomEmbed,
				(currentPage, totalPages) -> String.format("Page %d of %d", currentPage, totalPages), 10, 15);

		MessageBuilder initialMessage = new MessageBuilder().setContent(
				String.format("Found %d character%s.", searchResults.size(), searchResults.size() > 1 ? "s" : ""));

		handler.sendInitialMessage(initialMessage, e);
	}

	private EmbedBuilder createCustomEmbed(SearchResult searchResult) {
		EmbedBuilder embed = MessageFormats.createCharacterEmbed(searchResult.character);
		embed.setDescription(searchResult.result + MessageFormats.EQUALISE);
		return embed;
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
			return null;
		}
	}

	private static class SearchResult {
		final Characters character;
		final String result;

		SearchResult(Characters character, String result) {
			this.character = character;
			this.result = result;
		}
	}
}