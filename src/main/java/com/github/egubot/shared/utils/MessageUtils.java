package com.github.egubot.shared.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.Messageable;

import com.github.egubot.info.ServerInfoUtilities;

public class MessageUtils {
	public static final Pattern channelPattern = Pattern.compile("<#(\\d+)>");
	public static final Pattern pingPattern = Pattern.compile("(<@&?\\d+>)");
	public static final Pattern userIDPattern = Pattern.compile("<@(\\d+)>");
	public static final Pattern roleIDPattern = Pattern.compile("<@&(\\d+)>");
	
	private MessageUtils() {
	}

	public static List<Messageable> getChannels(String st) {
		List<Messageable> list = new ArrayList<>(1);
		Matcher matcher = channelPattern.matcher(st);
		while (matcher.find()) {
			long channelID = Long.parseLong(matcher.group(1));
			list.add(ServerInfoUtilities.getTextableRegularServerChannel(channelID));
		}
		return list;
	}

	public static String getPings(String st){
		StringBuilder pings = new StringBuilder();
		Matcher matcher = pingPattern.matcher(st);
		while (matcher.find()) {
			pings.append(matcher.group(1));
			pings.append(" ");
		}
		return pings.toString();
	}
	
	public static List<String> getPingedRoles(String st){
		List<String> ids = new ArrayList<>();
		Matcher matcher = roleIDPattern.matcher(st);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}
	
	public static List<String> getPingedUsers(String st){
		List<String> ids = new ArrayList<>();
		Matcher matcher = userIDPattern.matcher(st);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}
	
	
}
