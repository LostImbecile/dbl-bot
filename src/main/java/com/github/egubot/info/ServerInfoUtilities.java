package com.github.egubot.info;

import java.util.HashMap;
import java.util.Map;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;

public class ServerInfoUtilities {
	private static final Map<Long, Long> channelServerMap = new HashMap<>();

	private ServerInfoUtilities() {
	}

	private static void addServer(long channelID, long serverID) {
		channelServerMap.put(channelID, serverID);
	}

	public static Server getServer(Message msg) {
		return msg.getServer().get();
	}

	public static long getServerID(Message msg) {
		long channelID = msg.getChannel().getId();
		long serverID = channelServerMap.getOrDefault(channelID, -1L);

		if (serverID == -1) {
			serverID = getServer(msg).getId();
			addServer(channelID, serverID);
		}

		return serverID;
	}
}
