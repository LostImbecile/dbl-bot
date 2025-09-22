package com.github.egubot.features;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.storage.LocalDataManager;

public class ServerNotificationsFeature {
	private static final LocalDataManager dataManager = new LocalDataManager("Server Notifications Configuration");
	
	private static final Map<Long, ServerNotificationConfig> serverConfigs = new ConcurrentHashMap<>();

	static {
		dataManager.initialise(true);
		
		for (String line : dataManager.getData()) {
			try {
				String[] parts = line.split("\\|");
				if (parts.length >= 4) {
					long serverID = Long.parseLong(parts[0]);
					boolean joinEnabled = Boolean.parseBoolean(parts[1]);
					boolean leaveEnabled = Boolean.parseBoolean(parts[2]);
					long channelID = Long.parseLong(parts[3]);
					
					serverConfigs.put(serverID, new ServerNotificationConfig(joinEnabled, leaveEnabled, channelID));
				}
			} catch (Exception e) {
			}
		}
	}

	public static void enableJoinNotifications(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = serverConfigs.getOrDefault(serverID, new ServerNotificationConfig());
		config.joinEnabled = true;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static void disableJoinNotifications(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = serverConfigs.getOrDefault(serverID, new ServerNotificationConfig());
		config.joinEnabled = false;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static void enableLeaveNotifications(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = serverConfigs.getOrDefault(serverID, new ServerNotificationConfig());
		config.leaveEnabled = true;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static void disableLeaveNotifications(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = serverConfigs.getOrDefault(serverID, new ServerNotificationConfig());
		config.leaveEnabled = false;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static boolean isJoinEnabled(long serverID) {
		return serverConfigs.getOrDefault(serverID, new ServerNotificationConfig()).joinEnabled;
	}

	public static boolean isLeaveEnabled(long serverID) {
		return serverConfigs.getOrDefault(serverID, new ServerNotificationConfig()).leaveEnabled;
	}

	public static void setChannel(Message msg, long channelID) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		ServerNotificationConfig config = serverConfigs.getOrDefault(serverID, new ServerNotificationConfig());
		config.channelID = channelID;
		serverConfigs.put(serverID, config);
		saveConfig(serverID, config);
	}

	public static Long getChannelID(long serverID) {
		ServerNotificationConfig config = serverConfigs.get(serverID);
		return config != null && config.channelID != 0 ? config.channelID : null;
	}

	public static TextChannel getChannel(Server server, long serverID) {
		Long channelID = getChannelID(serverID);
		if (channelID == null) {
			return null;
		}
		
		return server.getTextChannelById(channelID).orElse(null);
	}

	public static boolean hasValidConfiguration(long serverID) {
		ServerNotificationConfig config = serverConfigs.get(serverID);
		return config != null && config.channelID != 0 && (config.joinEnabled || config.leaveEnabled);
	}

	public static ServerNotificationConfig getConfig(long serverID) {
		return serverConfigs.get(serverID);
	}

	private static void saveConfig(long serverID, ServerNotificationConfig config) {
		dataManager.getData().removeIf(line -> line.startsWith(serverID + "|"));
		String configLine = serverID + "|" + config.joinEnabled + "|" + config.leaveEnabled + "|" + config.channelID;
		dataManager.getData().add(configLine);
		dataManager.writeData(null);
	}

	public static class ServerNotificationConfig {
		public boolean joinEnabled = false;
		public boolean leaveEnabled = false;
		public long channelID = 0;

		public ServerNotificationConfig() {}

		public ServerNotificationConfig(boolean joinEnabled, boolean leaveEnabled, long channelID) {
			this.joinEnabled = joinEnabled;
			this.leaveEnabled = leaveEnabled;
			this.channelID = channelID;
		}
	}
}