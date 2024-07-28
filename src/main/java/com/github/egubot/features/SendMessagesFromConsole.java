package com.github.egubot.features;

import java.util.NoSuchElementException;
import java.util.Scanner;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import com.github.egubot.logging.StreamRedirector;
import com.github.egubot.main.Bot;
import com.github.egubot.main.Main;
import com.github.egubot.managers.KeyManager;
import com.github.egubot.objects.Abbreviations;
import com.github.egubot.shared.Shared;

public class SendMessagesFromConsole {
	private static Abbreviations emojis = getEmojis();

	private SendMessagesFromConsole() {

	}

	public static void start() {
		DiscordApi api = Bot.getApi();
		Scanner in = Shared.getSystemInput();

		String testChannelID = getTestChannelID(api, in);

		try {
			String channelID = testChannelID, message = "";
			TextChannel channel = api.getTextChannelById(channelID).get();

			StreamRedirector.println("", """
				
				Enter messages you want to send. Notes:\
				
				1- Use %n to separate lines.\
				
				2- Paste the channel ID to switch to it.\
				
				3- Press enter without writing anything to exit.\
				
				4- Don't write emojis, mentions and the sort as is, they won't work, write their full name.""");

			while (true) {

				message = in.nextLine();

				if (message.isBlank() || message.equals("exit"))
					break;

				if (message.length() >= 17 && message.matches("[\\d+]+")) {
					if (api.getTextChannelById(message).isPresent())
						channel = api.getTextChannelById(message).get();
					continue;
				}

				message = emojis.replaceAbbreviations(message);

				message = addNewlines(message);

				if (message.contains("reply SET")) {

					sendReply(api, message, channel);

				} else if (message.contains("react SET")) {

					sendReaction(api, message, channel);

				} else {

					channel.sendMessage(message);

				}
			}
		} catch (NoSuchElementException e) {
			Main.logger.error(e);
			StreamRedirector.println("", "\nSomething invalid was entered. Program will need to exit.");
		}
	}

	private static String getTestChannelID(DiscordApi api, Scanner in) {
		String testChannelID = KeyManager.getID("Default_Message_Channel_ID");
		if (!api.getTextChannelById(testChannelID).isPresent()) {
			StreamRedirector.println("prompt", "No default starting channel was set, enter a channel ID below:");
			KeyManager.updateKeys("Default_Message_Channel_ID", in.nextLine(), KeyManager.idsFileName);
			testChannelID = KeyManager.getID("Default_Message_Channel_ID");
		}
		return testChannelID;
	}

	private static String addNewlines(String message) {
		String[] messageArray = message.split("%n");
		message = "";
		for (String element : messageArray) {
			message += "\n" + element;
		}
		return message;
	}

	private static void sendReaction(DiscordApi api, String message, TextChannel channel) {
		String channelID;
		String messageID;
		String[] messageArray;
		messageArray = message.split("SET");
		if (messageArray.length >= 4) {
			messageID = messageArray[1].replace(" ", "");
			channelID = messageArray[2].replace(" ", "");
			if (channelID.equalsIgnoreCase("channel"))
				channelID = channel.getIdAsString();

			try {
				api.getMessageById(messageID, api.getTextChannelById(channelID).get()).get()
						.addReaction(emojis.replaceReactionIds(messageArray[3]));

			} catch (Exception e) {
				StreamRedirector.println("", "Failed to send");
			}
		}

	}

	private static void sendReply(DiscordApi api, String message, TextChannel channel) {
		String channelID;
		String messageID;
		String[] messageArray;
		messageArray = message.split("SET");
		if (messageArray.length == 4) {
			message = messageArray[3];
			messageID = messageArray[1].replace(" ", "");
			channelID = messageArray[2].replace(" ", "");
			if (channelID.equalsIgnoreCase("channel"))
				channelID = channel.getIdAsString();

			try {
				api.getMessageById(messageID, api.getTextChannelById(channelID).get()).get().reply(message, false);
			} catch (Exception e) {
				StreamRedirector.println("", "Failed to send");
			}
		}
	}

	/*
	 * You should probably make a shared class that reads a file with
	 * all your abbreviations so it's easy to add and get them.
	 * Or you can have them stored online as with the other classes
	 * allowing you to add and remove them from there, which is easier
	 * since you won't have to copy anything.
	 * 
	 * I don't care enough to do that myself however.
	 */
	public static Abbreviations getEmojis() {
		Abbreviations emojis = new Abbreviations();

		emojis.addAbbreviation("shinsmug", "<:ShinSmug:792523496726331434>");
		emojis.addAbbreviation("friezasmug", "<:FriezaSmug:789862857206530098>");
		emojis.addAbbreviation("gokuhuh", "<:huh:1184466187938185286>");
		emojis.addAbbreviation("nohoney", "<:nohoney:879731892818174012>");
		emojis.addAbbreviation(":sad:", "<:sad:1020780174901522442>");
		emojis.addAbbreviation("pecansmug", "<:PikkonSmug:789966590649040946>");
		emojis.addAbbreviation("thisguy", "<:thisguy:955513164315918336>");
		emojis.addAbbreviation("pepelaugh", "<:pepelaughpoint:819703838533353482>");
		emojis.addAbbreviation("yeshoney", "<:yeshoney:797101097273131069>");
		emojis.addAbbreviation("turles", "<:Orenokachida:797720523127259166>");
		emojis.addAbbreviation("orang", "<:orang:1020780129129091232>");
		emojis.addAbbreviation("pepehm", "<:pepehime_orihime:1040386873840906290>");
		emojis.addAbbreviation("cooler1", "<a:saikyo:792521951100796958>");
		emojis.addAbbreviation("cooler2", "<a:da:792522031601287218>");
		emojis.addAbbreviation("IE", "<:InternetE:792524260454432768>");
		emojis.addAbbreviation("joea", "<:joea:1144008494568194099>");

		return emojis;
	}
}
