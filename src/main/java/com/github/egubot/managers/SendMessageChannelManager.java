package com.github.egubot.managers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.egubot.storage.LocalDataManager;

public class SendMessageChannelManager {

	private static final LocalDataManager dataManager = new LocalDataManager("Send Message Channels");
	private static final Set<Long> channelIDs = ConcurrentHashMap.newKeySet();

	static {
		dataManager.initialise(false);
		for (String s : dataManager.getData()) {
			try {
				channelIDs.add(Long.parseLong(s));
			} catch (NumberFormatException e) {
			}
		}
		dataManager.writeData(null);
	}

	public static void addChannel(long channelID) {
		if (!channelIDs.contains(channelID)) {
			channelIDs.add(channelID);
			dataManager.getData().add(String.valueOf(channelID));
			dataManager.writeData(null);
		}
	}

	public static void removeChannel(long channelID) {
		if (channelIDs.contains(channelID)) {
			channelIDs.remove(channelID);
			dataManager.getData().remove(String.valueOf(channelID));
			dataManager.writeData(null);
		}
	}

	public static boolean isChannelPresent(long channelID) {
		return channelIDs.contains(channelID);
	}

	public static Set<Long> getAllChannels() {
		return channelIDs;
	}
}
