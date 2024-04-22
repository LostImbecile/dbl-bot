package com.github.egubot.facades;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.objects.APIResponse;
import com.meta.llama3.Llama3AI;
import com.openai.chatgpt.ChatGPT;

public class Llama3Context {
	private static final Logger logger = LogManager.getLogger(Llama3Context.class.getName());
	private static List<String> conversation = Collections.synchronizedList(new LinkedList<String>());
	private static int lastTokens = 0;

	public static boolean respond(Message msg, String msgText) {
		try {
			conversation.add(Llama3AI.reformatInput(msgText, msg.getAuthor().getName()));
			APIResponse response = Llama3AI.sendRequest(msgText, msg.getAuthor().getName(), conversation);

			if (!response.isError()) {
				msg.getChannel().sendMessage(response.getResponse());
				conversation.add(ChatGPT.reformatInput(response.getResponse(), "assistant"));
				
				lastTokens = response.getPromptTokens();
				if (lastTokens > 7000) {
					for (int i = 0; i < 10; i++) {
						conversation.remove(0);
					}
				}
			}
			return true;
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}

	public static List<String> getConversation() {
		return conversation;
	}

	public static void setConversation(List<String> conversation) {
		Llama3Context.conversation = conversation;
	}

	public static int getLastTokens() {
		return lastTokens;
	}
}
