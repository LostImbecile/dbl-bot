package com.github.egubot.facades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.openai.chatgpt.ChatGPT;

public class ChatGPTFacade {
	private static final Logger logger = LogManager.getLogger(ChatGPTFacade.class.getName());
	private List<String> chatgptConversation = Collections.synchronizedList(new ArrayList<String>(20));
	private boolean isChatGPTOn = false;
	private String chatGPTActiveChannelID = "";
	
	public boolean checkCommands(Message msg, String msgText, String lowCaseTxt) {
		if (lowCaseTxt.equals("chatgpt activate")) {
			isChatGPTOn = true;
			return true;
		}

		if (isChatGPTOn) {
			if (lowCaseTxt.equals("chatgpt deactivate")) {
				isChatGPTOn = false;
				return true;
			}

			if (lowCaseTxt.matches("gpt(?s).*") || msg.getChannel().getIdAsString().equals(chatGPTActiveChannelID)) {
				if (lowCaseTxt.equals("gpt clear")) {
					msg.getChannel().sendMessage("Conversation cleared :thumbsup:");
					chatgptConversation.clear();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel on")) {
					chatGPTActiveChannelID = msg.getChannel().getIdAsString();
					return true;
				}

				if (lowCaseTxt.equals("gpt channel off")) {
					chatGPTActiveChannelID = "";
					return true;
				}

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

				return true;
			}

			if (msg.getAuthor().isYourself()) {
				chatgptConversation.add(ChatGPT.reformatInput(msgText, "assistant"));
				return false;
			}

			chatgptConversation.add(ChatGPT.reformatInput(msgText, msg.getAuthor().getName()));
		}
		return false;

	}
}
