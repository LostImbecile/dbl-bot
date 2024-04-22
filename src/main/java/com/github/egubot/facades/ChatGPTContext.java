package com.github.egubot.facades;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.openai.chatgpt.ChatGPT;

public class ChatGPTContext {
	private static final Logger logger = LogManager.getLogger(ChatGPTContext.class.getName());
	private static List<String> conversation = Collections.synchronizedList(new LinkedList<String>());
	private static boolean isChatGPTOn = false;
	private static String chatGPTActiveChannelID = "";
	
	public static boolean repond(Message msg, String msgText) {

		if (isChatGPTOn &&  (msg.getChannel().getIdAsString().equals(chatGPTActiveChannelID))) {
				respond(msg, msgText);
				return true;
		}
		return false;

	}

	public static void respond(Message msg, String msgText) {
		try {
			conversation.add(ChatGPT.reformatInput(msgText, msg.getAuthor().getName()));
			
			String[] response = ChatGPT.chatGPT(msgText, msg.getAuthor().getName(), conversation);
			msg.getChannel().sendMessage(response[0]);
			conversation.add(ChatGPT.reformatInput(response[0], "assistant"));
			// 4096 Token limit that includes sent messages
			// Important to stay under it
			try {
				if (Integer.parseInt(response[1]) > 3300) {

					for (int i = 0; i < 5; i++) {
						conversation.remove(0);
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
		return conversation;
	}

	public static void setChatgptConversation(List<String> chatgptConversation) {
		ChatGPTContext.conversation = chatgptConversation;
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
