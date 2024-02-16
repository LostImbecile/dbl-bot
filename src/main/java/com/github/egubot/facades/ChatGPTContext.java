package com.github.egubot.facades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.openai.chatgpt.ChatGPT;

public class ChatGPTContext {
	private static final Logger logger = LogManager.getLogger(ChatGPTContext.class.getName());
	private static List<String> chatgptConversation = Collections.synchronizedList(new ArrayList<String>(20));
	private static boolean isChatGPTOn = false;
	private static String chatGPTActiveChannelID = "";
	
	public static boolean repond(Message msg, String msgText) {

		if (isChatGPTOn) {

			if (msg.getChannel().getIdAsString().equals(chatGPTActiveChannelID)) {

				respond(msg, msgText);

				return true;
			}

			chatgptConversation.add(ChatGPT.reformatInput(msgText, msg.getAuthor().getName()));
		}
		return false;

	}

	public static void addAssistantResponse(Message msg, String msgText) {
		if (isChatGPTOn && msg.getAuthor().isYourself()) {
			chatgptConversation.add(ChatGPT.reformatInput(msgText, "assistant"));
		}
	}

	public static void respond(Message msg, String msgText) {
		try {
			String[] response = ChatGPT.chatGPT(msgText, msg.getAuthor().getName(), chatgptConversation);
			msg.getChannel().sendMessage(response[0]);
			// 4096 Token limit that includes sent messages
			// Important to stay under it
			try {
				if (Integer.parseInt(response[1]) > 3300) {

					for (int i = 0; i < 5; i++) {
						chatgptConversation.remove(0);
					}
				}
			} catch (Exception e1) {
				logger.error(e1);
			}
		} catch (Exception e1) {
			logger.error(e1);
		}
	}
	
	public static void toggleChatGPT() {
		isChatGPTOn = !isChatGPTOn;
	}

	public static List<String> getChatgptConversation() {
		return chatgptConversation;
	}

	public static void setChatgptConversation(List<String> chatgptConversation) {
		ChatGPTContext.chatgptConversation = chatgptConversation;
	}

	public static boolean isChatGPTOn() {
		return isChatGPTOn;
	}

	public static void setChatGPTOn(boolean isChatGPTOn) {
		ChatGPTContext.isChatGPTOn = isChatGPTOn;
	}

	public static String getChatGPTActiveChannelID() {
		return chatGPTActiveChannelID;
	}

	public static void setChatGPTActiveChannelID(String chatGPTActiveChannelID) {
		ChatGPTContext.chatGPTActiveChannelID = chatGPTActiveChannelID;
	}
}
