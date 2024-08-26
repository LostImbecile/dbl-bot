package com.github.egubot.features;

import java.util.HashSet;
import java.util.Set;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;

public class FixPopularSiteEmbeds {

	private static final Set<Long> disabledServers = new HashSet<>();

	private FixPopularSiteEmbeds() {
	}

	public static void disableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		disabledServers.add(serverID);
	}

	public static void enableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		disabledServers.remove(serverID);
	}

	public static boolean isServerDisabled(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		return disabledServers.contains(serverID);
	}

	public static boolean fixEmbed(Message msg, String msgText) {
		if (!isServerDisabled(msg) && msgText.contains("https://")) {
			boolean foundReplaceableLink = false;
			String newText = null;
			if (msgText.contains("x.com")) {
				foundReplaceableLink = true;
				newText = msgText.replace("https://x.com", "fixupx.com");
			} else if (msgText.contains("www.instagram.com")) {
				foundReplaceableLink = true;
				newText = msgText.replace("www.instagram.com", "www.ddinstagram.com");
			} else if (msgText.contains("www.tiktok.com")) {
				foundReplaceableLink = true;
				newText = msgText.replace("www.tiktok.com", "www.tnktok.com");
			} else if (msgText.contains("reddit.com")) {
				foundReplaceableLink = true;
				newText = msgText.replace("reddit.com", "rxddit.com");
			}

			if (foundReplaceableLink) {
				msg.delete();
				msg.getChannel().sendMessage(msg.getAuthor().getDisplayName() + ": " + newText);
			}
			return true;
		}
		return false;
	}
}