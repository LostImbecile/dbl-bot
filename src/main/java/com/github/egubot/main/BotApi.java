package com.github.egubot.main;

import org.javacord.api.DiscordApi;

public class BotApi {
	private static DiscordApi api = null;

	private BotApi(DiscordApi api) {
		setApi(api);
	}

	public static void initialise(DiscordApi api) {
		new BotApi(api);
	}

	private static void setApi(DiscordApi api) {
		BotApi.api = api;
	}

	public static DiscordApi getApi() {
		return api;
	}
}
