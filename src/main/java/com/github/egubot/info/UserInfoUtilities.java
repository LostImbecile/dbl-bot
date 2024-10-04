package com.github.egubot.info;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

public class UserInfoUtilities {
	
	private UserInfoUtilities() {
	}
	
	public static boolean isOwner(Message msg) {
		boolean isOwner = false;
		if (msg.getAuthor().isBotOwner() || msg.getAuthor().isTeamMember()) {
			isOwner = true;
		}
		return isOwner;
	}

	public static boolean isServerOwner(Message msg) {
		return !msg.isPrivateMessage() && ServerInfoUtilities.getServer(msg).getOwnerId() == msg.getAuthor().getId();
	}

	public static boolean isPrivilegedOwner(Message msg) {
		return isServerOwner(msg) || isOwner(msg);
	}
	
	public static boolean isAdmin(Message msg) {
		return msg.getAuthor().isServerAdmin();
	}
	
	public static boolean canManageServer(Message msg) {
		return msg.getAuthor().canManageServer();
	}
	
	public static boolean canManageRoles(Message msg) {
		return msg.getAuthor().canManageRolesOnServer();
	}
	
	public static boolean canManageChannels(Message msg) {
		return msg.getAuthor().canCreateChannelsOnServer();
	}

	public static boolean isUserEqual(String id1, String id2) {
		return id1.equals(id2);
	}

	public static boolean isUserEqual(MessageAuthor author, String id2) {
		return author.getIdAsString().equals(id2);
	}
}
