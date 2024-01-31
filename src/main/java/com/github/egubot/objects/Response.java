package com.github.egubot.objects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response implements Comparable<Object> {
	@SerializedName("id")
	private String id;
	@SerializedName("match_type")
	private String matchType = "-1";
	@SerializedName("rspns_type")
	private String responseType = "-1";
	@SerializedName("invoc_msg")
	private String invocationMessage = "-1";
	@SerializedName("rspns_msg")
	private String responseMessage = "No Response";
	@SerializedName("reactions")
	private List<String> reactions;
	@SerializedName("author")
	private String author = "-1";
	@SerializedName("attr")
	private Attributes attr = new Attributes();
	@SerializedName("channel_blacklist")
	private List<String> blacklistedChannelIds;
	@SerializedName("user_blacklist")
	private List<String> blacklistedUserIds;
	@SerializedName("user_whitelist")
	private List<String> whitelist;
	@SerializedName("usage")
	private int usage = 0;

	public Response() {

	}

	public Response(String matchType, String responseType, String invocMsg, String responseMsg, List<String> reactions,
			String id) {
		this.matchType = matchType;
		this.responseType = responseType;
		this.invocationMessage = invocMsg;
		this.responseMessage = responseMsg;
		this.reactions = reactions;
		this.id = id;
	}

	@Override
	/*
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * If used in sort, order is descending.
	 */
	public int compareTo(Object o) {
		if (o instanceof Response resp) {
			return Integer.compare(resp.getUsage(), this.usage);
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Response resp) {
			String st = this.getInvocMsg().toLowerCase().replace(" ", "");
			String st2 = resp.getInvocMsg().toLowerCase().replace(" ", "");

			return isInvocEqual(st, st2);
		}
		return false;
	}

	public boolean updateResponse(String argument) {
		String st;
		if (argument.contains("response:")) {
			st = argument.replace("response:", "");
			if (!st.isBlank()) {
				this.setResponseMessage(st);
				return true;
			}
		}
		if (argument.contains("attr:")) {
			st = argument.replace("attr:", "");
			String[] temp = st.split(",");
			for (String attribute : temp) {
				try {
					String name = attribute.substring(0, attribute.indexOf("="));
					boolean value = Boolean.parseBoolean(attribute.substring(attribute.indexOf("=") + 1).strip());
					this.getAttr().updateAttribute(name, value);
				} catch (Exception e1) {
				}
			}
			return true;
		}
		if (argument.contains("reactions:")) {
			st = argument.replace("reactions:", "");
			String[] temp = st.split(",");
			this.getReactions().clear();
			for (String reaction : temp) {
				this.getReactions().add(reaction);
			}
			return true;
		}
		if (argument.contains("blacklist:")) {
			st = argument.replace("blacklist:", "");
			if (st.contains("#"))
				this.getBlacklistedChannelIds().clear();
			if (st.contains("@"))
				this.getBlacklistedUserIds();
			String[] temp = st.split(",");
			for (String id : temp) {
				if (id.contains("#")) {
					this.getBlacklistedChannelIds().add(id);
				} else if (id.contains("@")) {
					this.getBlacklistedUserIds().add(id);
				}
			}
			return true;
		}
		if (argument.contains("whitelist:")) {
			st = argument.replace("whitelist:", "");

			this.getWhitelist().clear();

			String[] temp = st.split(",");
			for (String id : temp) {
				this.getWhitelist().add(id);
			}
			return true;
		}
		
		return false;
	}

	public static boolean isInvocEqual(String st, String st2) {
		return st.matches(st2) || st2.matches(st) || st.equals(st2);
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String getInvocMsg() {
		return invocationMessage;
	}

	public void setInvocationMessage(String invocationMessage) {
		this.invocationMessage = invocationMessage;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public List<String> getReactions() {
		return reactions;
	}

	public void setReactions(List<String> reactions) {
		this.reactions = reactions;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getAuthorID() {
		return author.split("-")[0];
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<String> getBlacklistedChannelIds() {
		return blacklistedChannelIds;
	}

	public void setBlacklistedChannelIds(List<String> blacklistedChannelIds) {
		this.blacklistedChannelIds = blacklistedChannelIds;
	}

	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void incrementUsage() {
		this.usage++;
	}

	public String toString() {
		return getMatchType() + " >> " + getResponseType() + " >> " + getInvocMsg() + " >> " + getResponseMessage();
	}

	public Attributes getAttr() {
		return attr;
	}

	public void setAttr(Attributes attr) {
		this.attr = attr;
	}

	public List<String> getBlacklistedUserIds() {
		return blacklistedUserIds;
	}

	public void setBlacklistedUserIds(List<String> blacklistedUserIds) {
		this.blacklistedUserIds = blacklistedUserIds;
	}

	public List<String> getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(List<String> whitelist) {
		this.whitelist = whitelist;
	}

	public String getInvocationMessage() {
		return invocationMessage;
	}
}
