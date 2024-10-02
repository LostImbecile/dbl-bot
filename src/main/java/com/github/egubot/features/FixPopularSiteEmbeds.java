package com.github.egubot.features;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.javacord.api.entity.message.Message;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.storage.LocalDataManager;

public class FixPopularSiteEmbeds {
	private static final LocalDataManager dataManager = new LocalDataManager("Embed Fix Disabled Servers");
	private static final Set<Long> disabledServers = ConcurrentHashMap.newKeySet();

	static {
		dataManager.initialise(true);
		for (String s : dataManager.getData()) {
			try {
				disabledServers.add(Long.parseLong(s));
			} catch (Exception e) {
			}
		}
	}

	public static void disableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (disabledServers.contains(serverID)) {
			disabledServers.add(serverID);
			dataManager.getData().remove(serverID + "");
			dataManager.writeData(null);
		}
	}

	public static void enableServer(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		if (!disabledServers.contains(serverID)) {
			disabledServers.remove(serverID);
			dataManager.getData().add(serverID + "");
			dataManager.writeData(null);
		}
	}

	public static boolean isServerDisabled(Message msg) {
		long serverID = ServerInfoUtilities.getServerID(msg);
		return disabledServers.contains(serverID);
	}

	public static boolean fixEmbed(Message msg, String msgText, boolean checkServer, boolean reply, boolean deleteMsg) {
		if ((checkServer && isServerDisabled(msg)) || !msgText.contains("https://")) {
			return false;
		}

		boolean foundReplaceableLink = false;
		String newText = null;

		if (msgText.contains("https://x.com")) {
			foundReplaceableLink = true;
			newText = msgText.replace("https://x.com", "https://fixupx.com");
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
			String text = msg.getAuthor().getDisplayName() + ": " + newText;
			if (deleteMsg)
				msg.delete();
			
			if (reply)
				msg.getReferencedMessage().ifPresentOrElse(t -> t.reply(text),
						() -> msg.getChannel().sendMessage(text));
			else
				msg.getChannel().sendMessage(text);
		}
		return true;
	}

	public static boolean fixEmbed(Message msg, String msgText) {
		return fixEmbed(msg, msgText, true, true, true);
	}
}