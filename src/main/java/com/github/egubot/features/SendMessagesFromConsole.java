package com.github.egubot.features;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import com.github.egubot.main.BotApi;
import com.github.egubot.main.KeyManager;
import com.github.egubot.objects.Abbreviations;
import com.github.egubot.shared.Shared;

public class SendMessagesFromConsole {
	
	private SendMessagesFromConsole() {
		
	}

	public static void start() {
		DiscordApi api = BotApi.getApi();
		Scanner in = Shared.getSystemInput();
		ArrayList<Abbreviations> emojis = (ArrayList<Abbreviations>) getEmojis();

		String testChannelID = KeyManager.getID("Test_Channel_ID");
		if (!api.getTextChannelById(testChannelID).isPresent()) {
			System.out.println("No default starting channel was set, enter a channel ID below:");
			KeyManager.updateKeys("Test_Channel_ID", in.nextLine(), KeyManager.IDS_FILE_NAME);
			testChannelID = KeyManager.getID("Test_Channel_ID");
		}

		try {
			String channelID = testChannelID, message = "";
			TextChannel channel = api.getTextChannelById(channelID).get();

			System.out.println("\nEnter messages you want to send. Notes:" + "\n1- Use %n to separate lines."
					+ "\n2- Paste the channel ID to switch to it."
					+ "\n3- Press enter without writing anything to exit."
					+ "\n4- Don't write emojis, mentions and the sort as is, they won't work, write their full name.");

			while (true) {

				message = in.nextLine();

				if (message.isBlank() || message.equals("\n") || message.equals("exit"))
					break;

				if (message.length() >= 17 && message.matches("[\\d+]+")) {
					if (api.getTextChannelById(message).isPresent())
						channel = api.getTextChannelById(message).get();
					continue;
				}

				message = Abbreviations.replaceAbbreviations(message, emojis);
				
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
			System.err.println("\nSomething invalid was entered. Program will need to exit.");
		}
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
				messageArray = messageArray[3].split(" "); // Note: Separate emojis by spaces
				for (String element : messageArray)
					api.getMessageById(messageID, api.getTextChannelById(channelID).get()).get()
							.addReaction(Abbreviations.getReactionId(element));

			} catch (Exception e) {
				System.out.println("Failed to send");
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
				api.getMessageById(messageID, api.getTextChannelById(channelID).get()).get().reply(message,
						false);
			} catch (Exception e) {
				System.out.println("Failed to send");
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
	private static List<Abbreviations> getEmojis() {
		ArrayList<Abbreviations> emojis = new ArrayList<>(0);

		emojis.add(new Abbreviations("shinsmug", "<:ShinSmug:792523496726331434>"));
		emojis.add(new Abbreviations("friezasmug", "<:FriezaSmug:789862857206530098>"));
		emojis.add(new Abbreviations("gokuhuh", "<:huh:1184466187938185286>"));
		emojis.add(new Abbreviations("nohoney", "<:nohoney:879731892818174012>"));
		emojis.add(new Abbreviations(":sad:", "<:sad:1020780174901522442>"));
		emojis.add(new Abbreviations("pecansmug", "<:PikkonSmug:789966590649040946>"));
		emojis.add(new Abbreviations("thisguy", "<:thisguy:955513164315918336>"));
		emojis.add(new Abbreviations("pepelaugh", "<:pepelaughpoint:819703838533353482>"));
		emojis.add(new Abbreviations("yeshoney", "<:yeshoney:797101097273131069>"));
		emojis.add(new Abbreviations("turles", "<:Orenokachida:797720523127259166>"));
		emojis.add(new Abbreviations("orang", "<:orang:1020780129129091232>"));
		emojis.add(new Abbreviations("pepehm", "<:pepehime_orihime:1040386873840906290>"));
		emojis.add(new Abbreviations("cooler1", "<a:saikyo:792521951100796958>"));
		emojis.add(new Abbreviations("cooler2", "<a:da:792522031601287218>"));
		emojis.add(new Abbreviations("IE", "<:InternetE:792524260454432768>"));
		emojis.add(new Abbreviations("joea", "<:joea:1144008494568194099>"));

		return emojis;
	}
}
