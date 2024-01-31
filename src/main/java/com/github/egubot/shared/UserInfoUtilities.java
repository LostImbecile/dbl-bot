package com.github.egubot.shared;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

public class UserInfoUtilities {
	public static boolean isOwner(Message msg) {
		boolean isOwner = false;
		if (msg.getAuthor().isBotOwner() || msg.getAuthor().isTeamMember()) {
			isOwner = true;
		}
		return isOwner;
	}

	public static boolean isServerOwner(Message msg) {
		return !msg.isPrivateMessage() && msg.getServer().get().getOwnerId() == msg.getAuthor().getId();
	}

	public static boolean isUserEqual(String id1, String id2) {
		return id1.equals(id2);
	}

	public static boolean isUserEqual(MessageAuthor author, String id2) {
		return author.getIdAsString().equals(id2);
	}
}
