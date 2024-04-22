package com.github.egubot.main;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

import com.github.egubot.storage.ConfigManager;

public class Bot {
	private static DiscordApi api = null;
	private static String prefix = null;
	private static User botUser = null;
	private static String botInvite = null;
	private static String botName = null;
	private static User ownerUser = null;

	private Bot() {
	}

	public static void setApi(DiscordApi api) {
		Bot.api = api;
		botUser = api.getYourself();
		botInvite = api.createBotInvite();
		botName = botUser.getName();
		setOwnerUser(api.getOwner().get().join());
	}
	
	public static String getName() {
		return botName;
	}

	public static String getInvite() {
		return botInvite;
	}

	public static DiscordApi getApi() {
		return api;
	}

	public static User getYourself() {
		return botUser;
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
		ConfigManager.setProperty("prefix",prefix);
	}

	public static User getOwnerUser() {
		return ownerUser;
	}

	public static void setOwnerUser(User ownerUser) {
		Bot.ownerUser = ownerUser;
	}
}
