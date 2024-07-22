package com.github.egubot.info;

import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

import com.github.egubot.main.Bot;

public class ServerInfoUtilities {
	private static final Map<Long, Long> channelServerMap = new HashMap<>();
	private static final Map<Long, Server> serverMap = new HashMap<>();

	private ServerInfoUtilities() {
	}

	private static void addChannelServer(long channelID, long serverID) {
		channelServerMap.put(channelID, serverID);
	}

	private static void addServer(long serverID, Server server) {
		if (server != null)
			serverMap.put(serverID, server);
	}

	public static Server getServer(long serverID) {
		Server server = serverMap.getOrDefault(serverID, null);

		if (server == null) {
			server = Bot.getApi().getServerById(serverID).orElse(null);
			addServer(serverID, server);
		}
		return server;
	}

	public static Server getServer(Message msg) {
		Server server = msg.getServer().orElse(null);
		if (server != null)
			addServer(server.getId(), server);
		return server;
	}

	public static Server getServer(ServerTextChannel channel) {
		Server server = channel.getServer();
		addServer(server.getId(), server);
		return server;
	}

	public static ServerVoiceChannel getConnectedVoiceChannel(long serverID) {
		Server server = getServer(serverID);
		if (server == null)
			return null;
		return server.getConnectedVoiceChannel(Bot.getYourself()).orElse(null);
	}

	public static ServerVoiceChannel getConnectedVoiceChannel(Message msg) {
		Server server = getServer(msg);
		if (server == null)
			return null;
		return server.getConnectedVoiceChannel(Bot.getYourself()).orElse(null);
	}

	public static long getServerID(Message msg) {
		long channelID = msg.getChannel().getId();
		long serverID = channelServerMap.getOrDefault(channelID, -1L);

		if (serverID == -1) {
			Server server = getServer(msg);
			if (server == null)
				return -1;
			serverID = server.getId();
			addChannelServer(channelID, serverID);
		}

		return serverID;
	}
}
