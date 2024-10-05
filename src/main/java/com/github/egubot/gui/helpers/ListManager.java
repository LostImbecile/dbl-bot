package com.github.egubot.gui.helpers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import com.github.egubot.managers.SendMessageChannelManager;
import com.github.egubot.managers.EmojiManager;
import com.github.egubot.objects.Abbreviations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListManager {
	private final DiscordApi api;
	private final Abbreviations savedEmojis;

	public ListManager(DiscordApi api) {
		this.api = api;
		this.savedEmojis = EmojiManager.getAllEmojis();
	}

	public List<String> getAllEmojis() {
		Abbreviations allEmojis = new Abbreviations();
		api.getCustomEmojis().forEach(emoji -> {
			String prefix = emoji.isAnimated() ? "<a:" : "<:";
			String fullEmojiString = prefix + emoji.getName() + ":" + emoji.getId() + ">";
			allEmojis.put(emoji.getName(), fullEmojiString);
		});
		return new ArrayList<>(allEmojis.getAbbreviationMap().keySet());
	}

	public List<String> getSavedEmojis() {
		return new ArrayList<>(savedEmojis.getAbbreviationMap().keySet());
	}

	public List<String> getAllChannels() {
		return api.getServers().stream().flatMap(server -> server.getChannels().stream())
				.filter(channel -> channel.asServerTextChannel().isPresent())
				.map(channel -> channel.asServerTextChannel().get())
				.filter(channel -> channel.canYouSee() && channel.canYouWrite()).map(this::getChannelName).distinct()
				.sorted().collect(Collectors.toList());
	}

	public List<String> getSavedChannels() {
		return SendMessageChannelManager.getAllChannels().stream().map(api::getServerTextChannelById)
				.filter(java.util.Optional::isPresent).map(java.util.Optional::get).map(this::getChannelName).sorted()
				.collect(Collectors.toList());
	}

	public String getChannelName(ServerTextChannel channel) {
		return channel.getServer().getName() + " -> " + channel.getName();
	}

	public List<String> filterList(List<String> list, String filter) {
		return list.stream().filter(item -> item.toLowerCase().contains(filter.toLowerCase()))
				.collect(Collectors.toList());
	}

	public Map<String, ServerTextChannel> getChannelMap() {
		return api.getServers().stream().flatMap(server -> server.getTextChannels().stream())
				.collect(Collectors.toMap(this::getChannelName, channel -> channel, (c1, c2) -> c1));
	}

	public Abbreviations getAllEmojisAbbreviations() {
		Abbreviations allEmojis = new Abbreviations();
		api.getCustomEmojis().forEach(emoji -> {
			String prefix = emoji.isAnimated() ? "<a:" : "<:";
			String fullEmojiString = prefix + emoji.getName() + ":" + emoji.getId() + ">";
			allEmojis.put(emoji.getName(), fullEmojiString);
		});
		return allEmojis;
	}

	public Abbreviations getSavedEmojisAbbreviations() {
		return savedEmojis;
	}
}