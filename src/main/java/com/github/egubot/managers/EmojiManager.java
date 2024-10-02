package com.github.egubot.managers;

import java.util.ArrayList;

import com.github.egubot.main.Bot;
import com.github.egubot.objects.Abbreviations;
import com.github.egubot.storage.LocalDataManager;

public class EmojiManager {
	private static final LocalDataManager dataManager = new LocalDataManager("Emojis");
	private static final Abbreviations emojiMap = new Abbreviations();

	static {
		dataManager.initialise(false);
		for (String line : new ArrayList<String>(dataManager.getData())) {
			String[] parts = line.split(" ", 2);
			if (parts.length == 2) {
				Bot.getApi().getCustomEmojiById(parts[1].replaceAll("\\D", "")).ifPresentOrElse(emoji -> {
					if (emoji.isKnownCustomEmoji())
						emojiMap.put(parts[0], parts[1]);
				}, () -> dataManager.getData().removeIf(s -> s.startsWith(parts[0] + " ")));
			}
		}
		dataManager.writeData(null);
	}

	public static String getEmoji(String abbreviation) {
		return emojiMap.get(abbreviation);
	}

	public static void addEmoji(String abbreviation, String emoji) {
		emojiMap.put(abbreviation, emoji);
		dataManager.getData().add(abbreviation + " " + emoji);
		dataManager.writeData(null);
	}

	public static void removeEmoji(String abbreviation) {
		emojiMap.remove(abbreviation);
		dataManager.getData().removeIf(s -> s.startsWith(abbreviation + " "));
		dataManager.writeData(null);
	}

	public static Abbreviations getAllEmojis() {
		return emojiMap;
	}
}
