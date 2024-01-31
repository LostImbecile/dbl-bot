package com.github.egubot.objects;

import com.google.gson.annotations.SerializedName;

public class Attributes {
	@SerializedName("ignore_author")
	private boolean ignoreAuthor;
	@SerializedName("ignore_owner")
	private boolean ignoreOwner;
	@SerializedName("ignore_admin")
	private boolean ignoreAdmin;
	@SerializedName("reply_to_author")
	private boolean replyToAuthor;
	@SerializedName("reply_to_owner")
	private boolean replyToOwner;
	@SerializedName("reply_to_admin")
	private boolean replyToAdmin;
	@SerializedName("reply_to_reply")
	private boolean replyToReply;
	@SerializedName("reply_to_whitelist")
	private boolean replyToWhitelist;
	@SerializedName("only_whitelist")
	private boolean onlyWhitelist;
	@SerializedName("ignore_user_blacklist")
	private boolean ignoreUserBlacklist;
	@SerializedName("ignore_channel_blacklist")
	private boolean ignoreChannelBlacklist;
	@SerializedName("deletable")
	private boolean deletable;
	@SerializedName("editable")
	private boolean editable;
	@SerializedName("disabled")
	private boolean disabled;

	public Attributes() {
		this.ignoreAuthor = false;
		this.ignoreOwner = false;
		this.ignoreAdmin = false;
		this.replyToAuthor = false;
		this.replyToOwner = false;
		this.replyToAdmin = false;
		this.replyToReply = true;
		this.replyToWhitelist = false;
		this.onlyWhitelist = false;
		this.ignoreUserBlacklist = false;
		this.ignoreChannelBlacklist = false;
		this.deletable = true;
		this.editable = true;
		this.disabled = false;
	}

	public void updateAttribute(String attributeName, boolean value) {
		String lowerAttributeName = attributeName.toLowerCase().strip();

		switch (lowerAttributeName) {
		case "ignore_author":
			ignoreAuthor = value;
			break;
		case "ignore_owner":
			ignoreOwner = value;
			break;
		case "ignore_admin":
			ignoreAdmin = value;
			break;
		case "reply_to_author":
			replyToAuthor = value;
			break;
		case "reply_to_admin":
			replyToAdmin = value;
			break;
		case "reply_to_reply":
			replyToReply = value;
			break;
		case "reply_to_whitelist":
			replyToWhitelist = value;
			break;
		case "only_whitelist":
			onlyWhitelist = value;
			break;
		case "ignore_user_blacklist":
			ignoreUserBlacklist = value;
			break;
		case "ignore_channel_blacklist":
			ignoreChannelBlacklist = value;
			break;
		case "deletable":
			deletable = value;
			break;
		case "disabled":
			disabled = value;
			break;
		case "editable":
			editable = value;
			break;
		default:
			break;
		}
	}

	public boolean isIgnoreAuthor() {
		return ignoreAuthor;
	}

	public void setIgnoreAuthor(boolean ignoreAuthor) {
		this.ignoreAuthor = ignoreAuthor;
	}

	public boolean isIgnoreOwner() {
		return ignoreOwner;
	}

	public void setIgnoreOwner(boolean ignoreOwner) {
		this.ignoreOwner = ignoreOwner;
	}

	public boolean isIgnoreAdmin() {
		return ignoreAdmin;
	}

	public void setIgnoreAdmin(boolean ignoreAdmin) {
		this.ignoreAdmin = ignoreAdmin;
	}

	public boolean isReplyToAuthor() {
		return replyToAuthor;
	}

	public void setReplyToAuthor(boolean replyToAuthor) {
		this.replyToAuthor = replyToAuthor;
	}

	public boolean isReplyToAdmin() {
		return replyToAdmin;
	}

	public void setReplyToAdmin(boolean replyToAdmin) {
		this.replyToAdmin = replyToAdmin;
	}

	public boolean isReplyToReply() {
		return replyToReply;
	}

	public void setReplyToReply(boolean replyToReply) {
		this.replyToReply = replyToReply;
	}

	public boolean isReplyToWhitelist() {
		return replyToWhitelist;
	}

	public void setReplyToWhitelist(boolean replyToWhitelist) {
		this.replyToWhitelist = replyToWhitelist;
	}

	public boolean isOnlyWhitelist() {
		return onlyWhitelist;
	}

	public void setOnlyWhitelist(boolean onlyWhitelist) {
		this.onlyWhitelist = onlyWhitelist;
	}

	public boolean isIgnoreUserBlacklist() {
		return ignoreUserBlacklist;
	}

	public void setIgnoreUserBlacklist(boolean ignoreUserBlacklist) {
		this.ignoreUserBlacklist = ignoreUserBlacklist;
	}

	public boolean isIgnoreChannelBlacklist() {
		return ignoreChannelBlacklist;
	}

	public void setIgnoreChannelBlacklist(boolean ignoreChannelBlacklist) {
		this.ignoreChannelBlacklist = ignoreChannelBlacklist;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isReplyToOwner() {
		return replyToOwner;
	}

	public void setReplyToOwner(boolean replyToOwner) {
		this.replyToOwner = replyToOwner;
	}
}
