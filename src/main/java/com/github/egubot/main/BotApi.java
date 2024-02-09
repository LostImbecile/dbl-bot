package com.github.egubot.main;

import org.javacord.api.DiscordApi;

public class BotApi {
	// In
	private static DiscordApi api = null;

	private BotApi() {
	}

	public static void setApi(DiscordApi api) {
		BotApi.api = api;
	}

	public static DiscordApi getApi() {
		return api;
	}
}
