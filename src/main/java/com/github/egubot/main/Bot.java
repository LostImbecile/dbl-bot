package com.github.egubot.main;

import org.javacord.api.DiscordApi;

import com.github.egubot.storage.ConfigManager;

public class Bot {
	private static DiscordApi api = null;
	private static String prefix = null;

	private Bot() {
	}

	public static void setApi(DiscordApi api) {
		Bot.api = api;
	}

	public static DiscordApi getApi() {
		return api;
	}

	// No real performance hit in new JVM versions
	public static synchronized String getPrefix() {
		if (prefix == null) {
			String tempPrefix = ConfigManager.getProperty("prefix");
			if (tempPrefix == null || tempPrefix.isBlank()) {
				tempPrefix = "b-";
				ConfigManager.setProperty("prefix", tempPrefix);
			}
			prefix = tempPrefix.toLowerCase();
		}
		return prefix;

	}

	public static synchronized void setPrefix(String prefix) {
		Bot.prefix = prefix;
	}
}
