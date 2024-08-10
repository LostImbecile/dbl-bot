package com.github.egubot.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;

import com.github.egubot.info.ServerInfoUtilities;
import com.github.egubot.storage.DataManagerHandler;

public class LegendsNews extends DataManagerHandler {
	private static final Pattern channelPattern = Pattern.compile("<#(\\d+)>");
	private static final Pattern pingPattern = Pattern.compile("(<@&?\\d+>)");

	public LegendsNews() throws IOException {
		super("Legends News");
	}

	public void registerServer(Message msg, String args) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		for (String st : getData()) {
			if (st.startsWith(serverID)) {
				msg.getChannel().sendMessage("Server already registered");
				return;
			}
		}
		getData().add(serverID + " " + args);
		writeData(msg.getChannel());
	}

	public void removeServer(Message msg) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		List<String> data = getData();
		for (int i = 0; i < data.size(); i++) {
			String st = data.get(i);
			if (st.startsWith(serverID)) {
				data.remove(i);
				writeData(msg.getChannel());
				return;
			}
		}
	}

	public void update(Message msg, String args) {
		String serverID = ServerInfoUtilities.getServerID(msg) + "";
		for (int i = 0; i < getData().size(); i++) {
			String st = getData().get(i);
			if (st.startsWith(serverID)) {
				getData().set(i, serverID + " " + args);
				writeData(msg.getChannel());
				return;
			}
		}
	}

	public static List<Messageable> getChannels(String st) {
		List<Messageable> list = new ArrayList<>();
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
}
