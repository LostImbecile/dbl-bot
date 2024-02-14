package com.github.egubot.main;

import org.javacord.api.DiscordApi;

public class Bot {
	private static DiscordApi api = null;
	private static String prefix;

	private Bot() {
	}

	public static void setApi(DiscordApi api) {
		Bot.api = api;
	}

	public static DiscordApi getApi() {
		return api;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static void setPrefix(String prefix) {
		Bot.prefix = prefix;
	}
}
