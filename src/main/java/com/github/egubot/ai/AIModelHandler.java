package com.github.egubot.ai;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;

import com.github.egubot.objects.APIResponse;

public class AIModelHandler {
	private static final Logger logger = LogManager.getLogger(AIModelHandler.class.getName());
	private List<String> conversation = Collections.synchronizedList(new LinkedList<String>());
	private boolean isAIOn = false;
	private String activeChannelID = "";
	private int lastTokens = 0;
	private AIModel model = null;

	public AIModelHandler(AIModel model) {
		this.model = model;
	}

	public boolean respondIfChannelActive(Message msg, String msgText) {

		if (isAIOn && (msg.getChannel().getIdAsString().equals(activeChannelID))) {
			return respond(msg, msgText);
		}
		return false;

	}

	public boolean respond(Message msg, String msgText) {
		try {
			conversation.add(AIModel.reformatInput(msgText, msg.getAuthor().getName()));
			APIResponse response = getModel().sendRequest(msgText, msg.getAuthor().getName(), conversation);

			if (!response.isError()) {
				msg.getChannel().sendMessage(response.getResponse());
				conversation.add(AIModel.reformatInput(response.getResponse(), "assistant"));

				lastTokens = response.getTotalTokens();
				if (lastTokens > getModel().getTokenLimit() - 1000) {
					int deleteCount = Math.min(getModel().getTokenLimit() / 4096, 5);
					for (int i = 0; i < deleteCount; i++) {
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

	public List<String> getConversation() {
		return conversation;
	}

	public void setConversation(List<String> conversation) {
		this.conversation = conversation;
	}

	public int getLastTokens() {
		return lastTokens;
	}

	public AIModel getModel() {
		return model;
	}

	public boolean isAIOn() {
		return isAIOn;
	}

	public void setAIOn(boolean isChatGPTOn) {
		isAIOn = isChatGPTOn;
	}

	public void toggle() {
		isAIOn = !isAIOn;
	}

	public String getActiveChannelID() {
		return activeChannelID;
	}

	public void setActiveChannelID(String chatGPTActiveChannelID) {
		activeChannelID = chatGPTActiveChannelID;
	}

	public void setModel(AIModel model) {
		this.model = model;
	}

}