package com.github.egubot.shared.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.javacord.api.entity.message.Messageable;

import com.github.egubot.build.LegendsNews;
import com.github.egubot.info.ServerInfoUtilities;

public class MessageUtils {
	private MessageUtils() {
	}

	public static List<Messageable> getChannels(String st) {
		List<Messageable> list = new ArrayList<>();
		Matcher matcher = LegendsNews.channelPattern.matcher(st);
		while (matcher.find()) {
			long channelID = Long.parseLong(matcher.group(1));
			list.add(ServerInfoUtilities.getTextableRegularServerChannel(channelID));
		}
		return list;
	}

	public static String getPings(String st){
		StringBuilder pings = new StringBuilder();
		Matcher matcher = LegendsNews.pingPattern.matcher(st);
		while (matcher.find()) {
			pings.append(matcher.group(1));
			pings.append(" ");
		}
		return pings.toString();
	}
	
	
}
