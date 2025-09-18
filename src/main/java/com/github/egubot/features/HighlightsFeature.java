package com.github.egubot.features;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.storage.LocalDataManager;

public class HighlightsFeature {
	private static final LocalDataManager dataManager = new LocalDataManager("Highlights Configuration");
	
	private static final Map<Long, ServerHighlightsConfig> serverConfigs = new ConcurrentHashMap<>();

	static {
		dataManager.initialise(true);
		
		for (String line : dataManager.getData()) {
			try {
				String[] parts = line.split("\\|");
				if (parts.length >= 4) {
					long serverID = Long.parseLong(parts[0]);
					boolean enabled = Boolean.parseBoolean(parts[1]);
					String emoji = parts[2];
					long channelID = Long.parseLong(parts[3]);
					int threshold = parts.length > 4 ? Integer.parseInt(parts[4]) : 5;
					
					serverConfigs.put(serverID, new ServerHighlightsConfig(enabled, emoji, channelID, threshold));
				}
			} catch (Exception e) {
			}
		}
	}

	public static void enableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig());
		config.enabled = true;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static void disableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig());
		config.enabled = false;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static boolean isServerEnabled(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		return serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig()).enabled;
	}

	public static void setEmoji(Message msg, String emoji) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig());
		config.emoji = emoji;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static String getEmoji(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		return serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig()).emoji;
	}

	public static void setChannel(Message msg, long channelID) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig());
		config.channelID = channelID;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static Long getChannelID(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.get(serverID);
		return config != null && config.channelID != 0 ? config.channelID : null;
	}

	public static void setThreshold(Message msg, int threshold) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig());
		config.threshold = threshold;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static int getThreshold(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		return serverConfigs.getOrDefault(serverID, new ServerHighlightsConfig()).threshold;
	}

	public static TextChannel getChannel(Message msg) {
		Long channelID = getChannelID(msg);
		if (channelID == null) {
			return null;
		}
		
		Server server = msg.getServer().orElse(null);
		if (server == null) {
			return null;
		}
		
		return server.getTextChannelById(channelID).orElse(null);
	}

	public static boolean isValidConfiguration(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerHighlightsConfig config = serverConfigs.get(serverID);
		return config != null && config.enabled && config.emoji != null && config.channelID != 0;
	}

	private static void saveConfig(long serverID, ServerHighlightsConfig config) {
		dataManager.getData().removeIf(line -> line.startsWith(serverID + "|"));
		String configLine = serverID + "|" + config.enabled + "|" + config.emoji + "|" + config.channelID + "|" + config.threshold;
		dataManager.getData().add(configLine);
		dataManager.writeData(null);
	}

	private static class ServerHighlightsConfig {
		boolean enabled = false;
		String emoji = null;
		long channelID = 0;
		int threshold = 5;

		ServerHighlightsConfig() {}

		ServerHighlightsConfig(boolean enabled, String emoji, long channelID, int threshold) {
			this.enabled = enabled;
			this.emoji = emoji;
			this.channelID = channelID;
			this.threshold = threshold;
		}
	}
}